package com.craft.ai.codecraft.core.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * npm 镜像源地址，生产环境可指向私有源。
     * 默认使用 npmmirror 国内镜像，避免 Linux 服务器走官方源超时。
     */
    @Value("${npm.registry:https://registry.npmmirror.com}")
    private String npmRegistry;

    /**
     * 异步构建 vue 项目（不阻塞主进程）
     *
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath){
        Thread.ofVirtual().name("vue-builder-"+System.currentTimeMillis()).start(()->{
            try{
                buildProject(projectPath);
            }catch(Exception e){
                log.error("异步构建vue项目时发生异常: {}",e.getMessage(),e);
            }
        });
    }

    /**
     * 构建 vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if(!projectDir.exists()||!projectDir.isDirectory()){
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }
        //检查 package.json 项目是否存在
        File packageJson = new File(projectDir, "package.json");
        if(!packageJson.exists()){
            log.error("package.json 文件不存在：{}",packageJson.getAbsolutePath());
            return false;
        }
        //执行 npm install
        log.info("开始构建 vue 项目：{}", projectPath);
        if(!executeNpmInstall(projectDir)){
            log.error("npm install 执行失败");
            return false;
        }
        //执行 npm run build
        if(!executeNpmBuild(projectDir)){
            log.error("npm run build 执行失败");
            return false;
        }
        //验证 dist 目录是否存在
        File distDir = new File(projectDir, "dist");
        if(!distDir.exists()){
            log.error("构建完成但 dist 目录不存在：{}", distDir.getAbsolutePath());
            return false;
        }
        log.info("构建完成，dist 目录：{}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * 执行命令
     *
     * 关键修复：必须消费进程的 stdout/stderr，否则当输出写满 OS 管道缓冲区
     * （Linux 默认 64KB）时，子进程会阻塞在写输出上不退出，导致 waitFor 超时假死。
     * 这里合并 stdout 与 stderr，由守护线程持续读取并记录日志。
     *
     * @param workingDir     工作目录
     * @param command        命令（按空格分割为 token 数组）
     * @param timeoutSeconds 超时时间(秒)
     * @return 命令执行结果
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        Process process = null;
        Thread outputReader = null;
        try {
            log.info("在目录中 {} 执行命令：{}", workingDir.getAbsolutePath(), command);
            ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
            pb.directory(workingDir);
            // 合并 stderr 到 stdout，单流读取即可避免任一管道阻塞
            pb.redirectErrorStream(true);
            process = pb.start();

            final Process p = process;
            outputReader = Thread.ofVirtual().name("npm-output-reader").start(() -> {
                try (InputStream is = p.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[npm] {}", line);
                    }
                } catch (Exception e) {
                    log.warn("读取进程输出失败：{}", e.getMessage());
                }
            });

            //等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                // 等待输出线程结束，避免泄漏
                outputReader.join(2000);
                return false;
            }
            // 输出读完后再取退出码
            outputReader.join(5000);
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功：{}", command);
                return true;
            } else {
                log.error("命令执行失败：{}，错误码：{}", command, exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败：{}，错误信息：{}", command, e.getMessage());
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 执行npm install
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install，使用镜像源：{}", npmRegistry);
        // 通过 --registry 指定镜像源，避免 Linux 服务器走官方源下载缓慢
        String command = String.format("%s install --registry=%s", buildCommand("npm"), npmRegistry);
        return executeCommand(projectDir, command, 300);//5分钟超时
    }

    /**
     * 执行npm build
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run Build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180);//3分钟超时
    }

    /**
     * 判断当前操作系统是否是Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }
}
