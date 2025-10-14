package org.example.student_testing.student.service;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.student_testing.student.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {




    public static List<StudentDTO> readStudentsFromExcel(MultipartFile file) throws IOException {
        List<StudentDTO> list = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            StudentDTO dto = new StudentDTO();
            dto.setStudentId(row.getCell(0).getStringCellValue());
            dto.setFullName(row.getCell(1).getStringCellValue());
            dto.setEmail(row.getCell(2).getStringCellValue());
            dto.setCourseId((int) row.getCell(3).getNumericCellValue());
            dto.setStatus(row.getCell(4).getStringCellValue());

            list.add(dto);
        }

        workbook.close();
        return list;
    }

    public static Workbook generateStudentExcel(List<StudentDTO> students) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");


        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student ID");
        header.createCell(1).setCellValue("Full Name");
        header.createCell(2).setCellValue("Dob");
        header.createCell(3).setCellValue("Gender");
        header.createCell(4).setCellValue("Email");
        header.createCell(5).setCellValue("Course Name");
        header.createCell(6).setCellValue("Status");

        int rowNum = 1;
        for (StudentDTO s : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(s.getStudentId());
            row.createCell(1).setCellValue(s.getFullName());

            if (s.getDob() != null) {
                Cell dobCell = row.createCell(2);
                dobCell.setCellValue(s.getDob());
                dobCell.setCellStyle(dateStyle);
            } else {
                row.createCell(2).setCellValue("");
            }
            row.createCell(3).setCellValue(s.getGender());
            row.createCell(4).setCellValue(s.getEmail());
            row.createCell(5).setCellValue(
                    s.getCourseName() != null ? s.getCourseName() : ""
            );
            row.createCell(6).setCellValue(s.getStatus());
        }

        for (int i = 0; i <= 6; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }


}
