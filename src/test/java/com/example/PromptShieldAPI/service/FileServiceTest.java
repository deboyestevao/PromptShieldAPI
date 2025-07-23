package com.example.PromptShieldAPI.service;

import com.example.PromptShieldAPI.util.DataMasker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;

    @BeforeEach
    void setUp() throws IOException {
        fileService = new FileService(); // usa o construtor real
    }

    @Test
    void shouldLoadMaskedFileContentAndRespectTokenLimit(@TempDir Path tempDir) throws Exception {
        String userId = "testuser";
        String fileId = "abc123";
        String originalText = "Email: test@test.com\nCC: 1234-5678-9012-3456";
        String maskedText = "Email: ***@***.com\nCC: ****-****-****-****";

        Path userPath = tempDir.resolve(userId);
        Files.createDirectories(userPath);

        File file = new File(userPath.toFile(), fileId + "_teste.txt");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(originalText);
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenReturn(new MaskingResult(maskedText, 2L));

            String result = fileService.loadFilesContent(userPath.toString(), List.of(fileId));

            assertTrue(result.contains(maskedText));
            assertTrue(result.contains("Conteúdo de teste.txt"));
        }
    }

    @Test
    void shouldReturnErrorWhenUserFolderNotFound(@TempDir Path tempDir) {
        String result = fileService.loadFilesContent(tempDir.resolve("user_not_found").toString(), List.of("missing_file"));
        assertTrue(result.contains("não encontrada"));
    }

    @Test
    void shouldLoadMultipleFileTypesCorrectly() throws Exception {
        // Arrange
        String userId = "multiFileUser";
        String fileIdTxt = "file1";
        String fileIdPdf = "file2";
        String contentTxt = "Texto simples";
        String contentPdf = "Conteúdo PDF simulado";

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        // Cria ficheiro .txt
        File txtFile = new File(userFolder, fileIdTxt + "_test.txt");
        try (FileWriter fw = new FileWriter(txtFile)) {
            fw.write(contentTxt);
        }

        // Cria ficheiro .pdf válido
        File pdfFile = new File(userFolder, fileIdPdf + "_test.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(contentPdf);
                contentStream.endText();
            }

            document.save(pdfFile);
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenAnswer(invocation -> new MaskingResult(invocation.getArgument(0), 1L));

            // Act
            String result = fileService.loadFilesContent(userId, List.of(fileIdTxt, fileIdPdf));

            // Assert
            assertTrue(result.contains(contentTxt));
            assertTrue(result.contains(contentPdf));
        }

        txtFile.delete();
        pdfFile.delete();
        userFolder.delete();
    }

    @Test
    void shouldStopProcessingWhenTokenLimitExceeded() throws Exception {
        String userId = "tokenLimitUser";
        String fileId = "bigfile";
        StringBuilder bigContent = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            bigContent.append("data ");
        }

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        File file = new File(userFolder, fileId + "_big.txt");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(bigContent.toString());
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenReturn(new MaskingResult(bigContent.toString(), 5000L));

            String result = fileService.loadFilesContent(userId, List.of(fileId));

            assertTrue(result.contains("[Limite de tokens atingido"));
        }

        file.delete();
        userFolder.delete();
    }

    @Test
    void shouldHandleExceptionWhenReadingFile() throws Exception {
        String userId = "errorUser";
        String fileId = "errorfile";

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        File file = new File(userFolder, fileId + "_error.txt");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("conteúdo");
        }

        // Substitui temporariamente o ficheiro por um diretório com o mesmo nome para causar erro
        file.delete();
        file.mkdir(); // Isto causará erro ao tentar ler como ficheiro

        String result = fileService.loadFilesContent(userId, List.of(fileId));

        assertTrue(result.contains("Erro a ler o ficheiro"));

        // Limpeza
        file.delete();
        userFolder.delete();
    }

    @Test
    void shouldLoadDocxFileCorrectly() throws Exception {
        String userId = "docxUser";
        String fileId = "docxfile";
        String docxText = "Texto no Word";

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        File docxFile = new File(userFolder, fileId + "_test.docx");
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText(docxText);
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                doc.write(out);
            }
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenReturn(new MaskingResult(docxText, 1L));

            String result = fileService.loadFilesContent(userId, List.of(fileId));
            assertTrue(result.contains(docxText));
        }

        docxFile.delete();
        userFolder.delete();
    }

    @Test
    void shouldLoadXlsxFileCorrectly() throws Exception {
        String userId = "xlsxUser";
        String fileId = "xlsxfile";

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        File xlsxFile = new File(userFolder, fileId + "_test.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dados");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Nome");
            row.createCell(1).setCellValue("Idade");

            try (FileOutputStream out = new FileOutputStream(xlsxFile)) {
                workbook.write(out);
            }
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenReturn(new MaskingResult("Sheet: Dados\nNome | Idade | \n", 1L));

            String result = fileService.loadFilesContent(userId, List.of(fileId));
            assertTrue(result.contains("Sheet: Dados"));
        }

        xlsxFile.delete();
        userFolder.delete();
    }

    @Test
    void shouldLoadPptxFileCorrectly() throws Exception {
        String userId = "pptxUser";
        String fileId = "pptxfile";

        Path userPath = fileService.getUploadDir().resolve(userId);
        File userFolder = userPath.toFile();
        userFolder.mkdirs();

        File pptxFile = new File(userFolder, fileId + "_test.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextShape shape = slide.createTextBox();
            shape.setText("Texto no slide");

            try (FileOutputStream out = new FileOutputStream(pptxFile)) {
                ppt.write(out);
            }
        }

        try (MockedStatic<DataMasker> mocked = Mockito.mockStatic(DataMasker.class)) {
            mocked.when(() -> DataMasker.maskSensitiveData(Mockito.anyString()))
                    .thenReturn(new MaskingResult("Slide 1:\nTexto no slide\n", 1L));

            String result = fileService.loadFilesContent(userId, List.of(fileId));
            assertTrue(result.contains("Texto no slide"));
        }

        pptxFile.delete();
        userFolder.delete();
    }

    @Test
    void shouldEstimateTokensCorrectly() {
        assertEquals(1, fileService.estimateTokens("1234"));        // 4 chars → 1 token
        assertEquals(2, fileService.estimateTokens("12345678"));    // 8 chars → 2 tokens
        assertEquals(0, fileService.estimateTokens(""));            // 0 chars → 0 tokens
    }


    @Test
    void shouldSaveFilesSuccessfully(@TempDir Path tempDir) throws Exception {
        String userId = "saveUser";
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn("teste.txt");
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("conteúdo".getBytes()));

        List<String> ids = fileService.saveFiles(new MultipartFile[]{mockFile}, userId);
        assertEquals(1, ids.size());
    }

    @Test
    void shouldHandleIOExceptionWhenSavingFile(@TempDir Path tempDir) throws Exception {
        String userId = "errorSaveUser";
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("erro.txt");
        Mockito.when(file.getInputStream()).thenThrow(new IOException("Erro simulado"));

        List<String> ids = fileService.saveFiles(new MultipartFile[]{file}, userId);
        assertTrue(ids.isEmpty());
    }

}