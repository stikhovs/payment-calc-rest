package ru.payment.calc.payment_calculator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.payment.calc.payment_calculator.props.FileProps;
import ru.payment.calc.payment_calculator.service.FileRemoveService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileRemoveServiceImpl implements FileRemoveService {

    private final FileProps fileProps;

    @Override
    @SneakyThrows
    @Scheduled(fixedDelay = 1000 * 60) // 1 minute
    public void removeOldFiles() {
        cleanUp(Path.of(fileProps.getSourceWorkbookDirectory()));
        cleanUp(Path.of(fileProps.getResultWorkbookDirectory()));
    }

    private void cleanUp(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.find(path, 1, (filePath, basicFileAttributes) -> {
                FileTime creationTime = basicFileAttributes.creationTime();
                return basicFileAttributes.isRegularFile() && isTimeExceeded(creationTime);
            })
                    .forEach(this::deleteFile);
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
            log.info("Successfully deleted during scheduled cleaning: {}", path.getFileName());
        } catch (IOException e) {
            log.error("Couldn't delete during scheduled cleaning: {}", path);
            e.printStackTrace();
        }
    }

    private boolean isTimeExceeded(FileTime creationTime) {
        long fileLiveTime = Duration
                .between(creationTime.toInstant(), Instant.now())
                .toMinutes();
        long availableTimeToLive = fileProps.getAvailableTimeToLive();

        return fileLiveTime >= availableTimeToLive;
    }

}
