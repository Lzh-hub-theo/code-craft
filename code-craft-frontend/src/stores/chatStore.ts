import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/request'
import { API_BASE_URL } from '@/config/env'

/**
 * 对话消息结构（前端内部使用）
 */
export interface ChatMessage {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
}

/**
 * ChatStore 设计要点：
 * 1. 消息与"是否正在生成"按 appId 隔离，方便在多个应用之间切换时不串数据。
 * 2. EventSource 实例保存在普通对象（不放入 ref），由浏览器的网络层保持连接，
 *    即便 AppChatPage 组件被卸载也不会被回收，从而让用户在生成中途切走再回来时
 *    仍能看到持续追加的 AI 回复。
 * 3. startStream 是幂等的：如果某个 appId 已经在生成中，再次调用会被忽略，避免
 *    用户重复点击发送按钮导致后端被多次扣减 token / 出现重复流。
 */
export const useChatStore = defineStore('chat', () => {
  // 按 appId 存放的消息列表（reactive，保证组件能响应式渲染）
  const messagesMap = ref<Record<string, ChatMessage[]>>({})
  // 按 appId 存放的"是否正在生成"状态
  const generatingMap = ref<Record<string, boolean>>({})
  // 按 appId 存放的"是否已经从后端加载过历史"标志，避免重复加载
  const historyLoadedMap = ref<Record<string, boolean>>({})

  // 非响应式的 EventSource 引用，单纯用于主动 close 以及并发控制
  const eventSources: Record<string, EventSource> = {}
  // 单条流是否已经结束（done / error / business-error 都会置 true）
  const streamCompletedFlags: Record<string, boolean> = {}

  function ensureBucket(appId: string): ChatMessage[] {
    if (!messagesMap.value[appId]) {
      messagesMap.value[appId] = []
    }
    return messagesMap.value[appId]
  }

  function getMessages(appId: string | number | undefined | null): ChatMessage[] {
    if (appId === undefined || appId === null || appId === '') return []
    return messagesMap.value[String(appId)] || []
  }

  function isGenerating(appId: string | number | undefined | null): boolean {
    if (appId === undefined || appId === null || appId === '') return false
    return !!generatingMap.value[String(appId)]
  }

  function hasHistoryLoaded(appId: string | number | undefined | null): boolean {
    if (appId === undefined || appId === null || appId === '') return false
    return !!historyLoadedMap.value[String(appId)]
  }

  function setMessages(appId: string | number, msgs: ChatMessage[]) {
    messagesMap.value[String(appId)] = [...msgs]
  }

  function appendMessage(appId: string | number, msg: ChatMessage) {
    ensureBucket(String(appId)).push(msg)
  }

  function prependMessages(appId: string | number, msgs: ChatMessage[]) {
    const bucket = ensureBucket(String(appId))
    bucket.unshift(...msgs)
  }

  function updateMessageContent(appId: string | number, index: number, content: string) {
    const bucket = messagesMap.value[String(appId)]
    if (bucket && bucket[index]) {
      bucket[index].content = content
      bucket[index].loading = false
    }
  }

  /**
   * 关闭指定 appId 的 EventSource（如果有），并清理相关标志位。
   * 不会清空 messagesMap，保留已经累计好的消息给 UI 继续渲染。
   */
  function closeStream(appId: string | number) {
    const key = String(appId)
    if (eventSources[key]) {
      try {
        eventSources[key].close()
      } catch {
        // ignore
      }
      delete eventSources[key]
    }
    generatingMap.value[key] = false
    streamCompletedFlags[key] = true
  }

  /**
   * 启动 SSE 流式生成。
   * - 幂等：已经在生成中则直接返回
   * - 自动追加用户消息 + AI 占位消息
   * - 即使组件被卸载，EventSource 仍由本 store 持有，连接保持打开，
   *   后续重新进入对话页时 messagesMap 中已经有最新的内容
   */
  function startStream(appId: string | number, userMessage: string) {
    const key = String(appId)
    if (generatingMap.value[key]) {
      // 已经在生成中，忽略重复请求
      return
    }

    // 添加用户消息和 AI 占位
    ensureBucket(key)
    appendMessage(key, { type: 'user', content: userMessage })
    const aiIndex = messagesMap.value[key].length
    appendMessage(key, { type: 'ai', content: '', loading: true })

    generatingMap.value[key] = true
    streamCompletedFlags[key] = false

    const baseURL = request.defaults.baseURL || API_BASE_URL
    const params = new URLSearchParams({
      appId: key,
      message: userMessage,
    })
    const url = `${baseURL}/app/chat/gen/code?${params}`

    let eventSource: EventSource
    try {
      eventSource = new EventSource(url, { withCredentials: true })
    } catch (err) {
      console.error('[chatStore] 创建 EventSource 失败：', err)
      const bucket = messagesMap.value[key]
      if (bucket && bucket[aiIndex]) {
        bucket[aiIndex].content = '抱歉，生成过程中出现了错误，请重试。'
        bucket[aiIndex].loading = false
      }
      generatingMap.value[key] = false
      streamCompletedFlags[key] = true
      return
    }
    eventSources[key] = eventSource

    let fullContent = ''

    eventSource.onmessage = (event: MessageEvent) => {
      if (streamCompletedFlags[key]) return
      try {
        const parsed = JSON.parse(event.data)
        const content = parsed?.d
        if (content !== undefined && content !== null) {
          fullContent += content
          updateMessageContent(key, aiIndex, fullContent)
        }
      } catch (err) {
        console.error('[chatStore] 解析消息失败：', err, event.data)
        const bucket = messagesMap.value[key]
        if (bucket && bucket[aiIndex]) {
          bucket[aiIndex].content = '抱歉，生成过程中出现了错误，请重试。'
          bucket[aiIndex].loading = false
        }
        closeStream(key)
      }
    }

    eventSource.addEventListener('done', () => {
      if (streamCompletedFlags[key]) return
      streamCompletedFlags[key] = true
      generatingMap.value[key] = false
      // 让后端有时间完成最终落盘，触发预览刷新（由组件监听 isGenerating 变化完成）
      try {
        eventSource.close()
      } catch {
        // ignore
      }
      delete eventSources[key]
    })

    eventSource.addEventListener('business-error', (event: MessageEvent) => {
      if (streamCompletedFlags[key]) return
      try {
        const errorData = JSON.parse(event.data)
        const errorMessage = errorData?.message || '生成过程中出现错误'
        const bucket = messagesMap.value[key]
        if (bucket && bucket[aiIndex]) {
          bucket[aiIndex].content = `❌ ${errorMessage}`
          bucket[aiIndex].loading = false
        }
      } catch (parseError) {
        console.error('[chatStore] 解析错误事件失败：', parseError, event.data)
        const bucket = messagesMap.value[key]
        if (bucket && bucket[aiIndex]) {
          bucket[aiIndex].content = '抱歉，生成过程中出现了错误，请重试。'
          bucket[aiIndex].loading = false
        }
      }
      closeStream(key)
    })

    eventSource.onerror = () => {
      if (streamCompletedFlags[key] || !generatingMap.value[key]) return
      // EventSource 在断线时会自动重连，此时 readyState 为 CONNECTING。
      // 原代码在此分支视为"正常关闭"，仅做收尾。
      if (eventSource.readyState === EventSource.CONNECTING) {
        streamCompletedFlags[key] = true
        generatingMap.value[key] = false
        try {
          eventSource.close()
        } catch {
          // ignore
        }
        delete eventSources[key]
      } else {
        const bucket = messagesMap.value[key]
        if (bucket && bucket[aiIndex]) {
          bucket[aiIndex].content = '抱歉，生成过程中出现了错误，请重试。'
          bucket[aiIndex].loading = false
        }
        closeStream(key)
      }
    }
  }

  /**
   * 完全清理某个 appId 在 store 中的所有痕迹（消息、生成状态、EventSource）。
   * 在删除应用时调用。
   */
  function clearApp(appId: string | number) {
    const key = String(appId)
    if (eventSources[key]) {
      try {
        eventSources[key].close()
      } catch {
        // ignore
      }
      delete eventSources[key]
    }
    delete streamCompletedFlags[key]
    delete messagesMap.value[key]
    delete generatingMap.value[key]
    delete historyLoadedMap.value[key]
  }

  return {
    messagesMap,
    generatingMap,
    historyLoadedMap,
    getMessages,
    isGenerating,
    hasHistoryLoaded,
    setMessages,
    appendMessage,
    prependMessages,
    updateMessageContent,
    startStream,
    closeStream,
    clearApp,
  }
})