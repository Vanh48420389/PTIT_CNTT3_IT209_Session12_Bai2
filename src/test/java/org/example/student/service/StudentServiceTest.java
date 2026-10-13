package org.example.student.service;

import org.example.student.dto.PageResponse;
import org.example.student.dto.StudentRequest;
import org.example.student.dto.StudentResponse;
import org.example.student.entity.Gender;
import org.example.student.entity.Student;
import org.example.student.exception.DuplicateResourceException;
import org.example.student.exception.ResourceNotFoundException;
import org.example.student.repository.StudentRepository;
import org.example.student.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private StudentRequest studentRequest;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2003, 5, 20))
                .gender(Gender.MALE)
                .phoneNumber("0912345678")
                .gpa(3.5)
                .build();

        studentRequest = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2003, 5, 20))
                .gender(Gender.MALE)
                .phoneNumber("0912345678")
                .gpa(3.5)
                .build();
    }

    @Test
    @DisplayName("Tạo sinh viên thành công")
    void testCreateStudent_Success() {
        when(studentRepository.existsByStudentCode("SV001")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.createStudent(studentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStudentCode()).isEqualTo("SV001");
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Tạo sinh viên thất bại khi trùng mã sinh viên")
    void testCreateStudent_DuplicateCode_ThrowsException() {
        when(studentRepository.existsByStudentCode("SV001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(studentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã sinh viên đã tồn tại: SV001");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Lấy sinh viên theo ID thành công")
    void testGetStudentById_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStudentCode()).isEqualTo("SV001");
    }

    @Test
    @DisplayName("Lấy sinh viên theo ID không tồn tại trả lỗi 404")
    void testGetStudentById_NotFound_ThrowsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy sinh viên với ID: 99");
    }

    @Test
    @DisplayName("Cập nhật thông tin sinh viên thành công")
    void testUpdateStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByStudentCodeAndIdNot("SV001", 1L)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.updateStudent(1L, studentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStudentCode()).isEqualTo("SV001");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Xóa sinh viên thành công")
    void testDeleteStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(studentRepository).delete(student);

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    @DisplayName("Lấy danh sách sinh viên phân trang thành công")
    void testGetAllStudents_Success() {
        Page<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> response = studentService.getAllStudents(
                "Nguyen", 3.0, 4.0, 0, 10, "id", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }
}
