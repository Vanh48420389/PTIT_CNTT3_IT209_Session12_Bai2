package org.example.student.service;

import org.example.student.dto.PageResponse;
import org.example.student.dto.StudentRequest;
import org.example.student.dto.StudentResponse;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    StudentResponse getStudentById(Long id);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);

    PageResponse<StudentResponse> getAllStudents(
            String keyword,
            Double minGpa,
            Double maxGpa,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
