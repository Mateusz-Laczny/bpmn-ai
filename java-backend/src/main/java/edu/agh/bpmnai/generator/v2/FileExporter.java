package edu.agh.bpmnai.generator.v2;

import edu.agh.bpmnai.generator.datatype.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.exists;

@Service
@Slf4j
public class FileExporter {
    public Result<Void, FileError> exportToFile(Path filepath, String contentToExport) {
        if (!exists(filepath)) {
            var createFileResult = createFile(filepath);
            if (createFileResult.isError()) {
                return Result.error(createFileResult.getError());
            }
        }

        try {
            Files.writeString(filepath, contentToExport);
            return Result.ok(null);
        } catch (IOException e) {
            log.warn("Could not write to file", e);
            return Result.error(FileError.CANT_WRITE_TO_FILE);
        }
    }

    private Result<Void, FileError> createFile(Path filepath) {
        try {
            Files.createFile(filepath);
            return Result.ok(null);
        } catch (IOException e) {
            log.warn("Could not create file", e);
            return Result.error(FileError.CANT_CREATE_FILE);
        }
    }
}
