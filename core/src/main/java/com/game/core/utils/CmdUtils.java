package com.game.core.utils;

import com.game.core.exception.BusinessException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Slf4j
public class CmdUtils {


    @SneakyThrows
    public static String executeCommand(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 手动指定编码为GBK
        Charset charset = Charset.forName("GBK");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            log.error("命令执行中断: " + String.join(" ", command), e);
            throw new BusinessException("命令执行中断: " + e.getMessage());
        }
        if (exitCode != 0) {
            log.error("命令执行失败: {}", String.join(" ", command));
            throw new BusinessException("命令执行失败");
        }
        return output.toString();
    }
}
