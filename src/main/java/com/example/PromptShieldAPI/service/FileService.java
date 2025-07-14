package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.util.DataMasker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Data
public class FileService {

    private final Path uploadDir = Paths.get("uploads");
    private static final int MAX_INPUT_TOKENS = 4096;

    public FileService() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public List<String> saveFiles(MultipartFile[] files, String userId) {
        List<String> fileIds = new ArrayList<>();

        Path userFolder = uploadDir.resolve(userId);
        try {
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return fileIds;
        }

        for (MultipartFile file : files) {
            try {
                String fileId = UUID.randomUUID().toString();
                String fileName = fileId + "_" + file.getOriginalFilename();

                Path targetPath = userFolder.resolve(fileName);
                Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                fileIds.add(fileId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileIds;
    }

    public int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }

    public String loadFilesContent(String userId, List<String> fileIds) {
        StringBuilder content = new StringBuilder();
        int totalTokens = 0;

        File userFolder = uploadDir.resolve(userId).toFile();
        if (!userFolder.exists() || !userFolder.isDirectory()) {
            System.out.println("Pasta do utilizador não existe: " + userFolder.getAbsolutePath());
            return "";
        }

        for (String id : fileIds) {
            File[] matchingFiles = userFolder.listFiles(file -> file.getName().startsWith(id));
            if (matchingFiles == null || matchingFiles.length == 0) {
                return "Ficheiro para id " + id + " não encontrado na pasta do utilizador.";
            }

            File file = matchingFiles[0];
            String fileName = file.getName().replaceFirst(id + "_", "");
            String lowerName = fileName.toLowerCase();

            try {
                String fileText;

                if (lowerName.endsWith(".docx")) {
                    try (FileInputStream fis = new FileInputStream(file); XWPFDocument document = new XWPFDocument(fis)) {
                        StringBuilder docText = new StringBuilder();
                        for (XWPFParagraph para : document.getParagraphs()) {
                            docText.append(para.getText()).append("\n");
                        }
                        fileText = docText.toString();
                    }
                } else if (lowerName.endsWith(".xlsx")) {
                    try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
                        StringBuilder excelText = new StringBuilder();
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            Sheet sheet = workbook.getSheetAt(i);
                            excelText.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                            for (Row row : sheet) {
                                for (Cell cell : row) {
                                    switch (cell.getCellType()) {
                                        case STRING -> excelText.append(cell.getStringCellValue());
                                        case NUMERIC -> excelText.append(cell.getNumericCellValue());
                                        case BOOLEAN -> excelText.append(cell.getBooleanCellValue());
                                        case FORMULA -> excelText.append(cell.getCellFormula());
                                        default -> excelText.append("");
                                    }
                                    excelText.append(" | ");
                                }
                                excelText.append("\n");
                            }
                            excelText.append("\n");
                        }
                        fileText = excelText.toString();
                    }
                } else if (lowerName.endsWith(".pdf")) {
                    try (PDDocument pdf = PDDocument.load(file)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        fileText = stripper.getText(pdf);
                    }
                } else if (lowerName.endsWith(".pptx")) {
                    try (FileInputStream fis = new FileInputStream(file); XMLSlideShow ppt = new XMLSlideShow(fis)) {
                        StringBuilder pptText = new StringBuilder();
                        int slideNum = 1;
                        for (XSLFSlide slide : ppt.getSlides()) {
                            pptText.append("Slide ").append(slideNum++).append(":\n");
                            for (XSLFShape shape : slide.getShapes()) {
                                if (shape instanceof XSLFTextShape textShape) {
                                    pptText.append(textShape.getText()).append("\n");
                                }
                            }
                            pptText.append("\n");
                        }
                        fileText = pptText.toString();
                    }
                } else {
                    // .txt, .csv, .json, etc.
                    fileText = new String(Files.readAllBytes(file.toPath()));
                }

                String masked = DataMasker.maskSensitiveData(fileText).getMaskedText();
                int tokens = estimateTokens(masked);

                if ((totalTokens + tokens) > MAX_INPUT_TOKENS) {
                    content.append("\n[Limite de tokens atingido. Conteúdo parcial processado.]\n");
                    break;
                }

                totalTokens += tokens;
                content.append("\n--- Conteúdo de ").append(fileName).append(" ---\n\n");
                content.append(masked).append("\n");

            } catch (Exception e) {
                content.append("\nErro a ler o ficheiro: ").append(file.getName())
                        .append(" - ").append(e.getMessage()).append("\n");
                e.printStackTrace();
            }
        }

        return content.toString();
    }
}
