package org.example.student.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.student.dto.PageResponse;
import org.example.student.dto.StudentRequest;
import org.example.student.dto.StudentResponse;
import org.example.student.entity.Student;
import org.example.student.exception.DuplicateResourceException;
import org.example.student.exception.ResourceNotFoundException;
import org.example.student.repository.StudentRepository;
import org.example.student.service.StudentService;
import org.example.student.specification.StudentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new DuplicateResourceException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }

        Student student = Student.builder()
                .studentCode(request.getStudentCode().trim())
                .fullName(request.getFullName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber().trim())
                .gpa(request.getGpa())
                .build();

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = findStudentById(id);
        return mapToResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudentById(id);

        if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode().trim(), id)) {
            throw new DuplicateResourceException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }

        student.setStudentCode(request.getStudentCode().trim());
        student.setFullName(request.getFullName().trim());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setPhoneNumber(request.getPhoneNumber().trim());
        student.setGpa(request.getGpa());

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudentById(id);
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getAllStudents(
            String keyword,
            Double minGpa,
            Double maxGpa,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Student> spec = StudentSpecification.filter(keyword, minGpa, maxGpa);
        Page<Student> studentPage = studentRepository.findAll(spec, pageable);

        Page<StudentResponse> responsePage = studentPage.map(this::mapToResponse);
        return PageResponse.from(responsePage);
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + id));
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .phoneNumber(student.getPhoneNumber())
                .gpa(student.getGpa())
                .build();
    }
}
