package org.example.student.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.student.entity.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StudentSpecification {

    public static Specification<Student> filter(String keyword, Double minGpa, Double maxGpa) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate fullNamePredicate = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate studentCodePredicate = cb.like(cb.lower(root.get("studentCode")), pattern);
                predicates.add(cb.or(fullNamePredicate, studentCodePredicate));
            }

            if (minGpa != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("gpa"), minGpa));
            }

            if (maxGpa != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("gpa"), maxGpa));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
