import { useState, useCallback } from "react";
import { studentApi } from "@/api/v1/modules/student";
import type { StudentDTO } from "@/types/student";

interface UseStudentReturn {
  // 状态
  student: StudentDTO | null;

  // 操作
  getStudentByUserId: (userId: string) => Promise<void>;
}

export function useStudent(): UseStudentReturn {
  const [student, setStudent] = useState<StudentDTO | null>(null);

  const getStudentByUserId = useCallback(async (userId: string) => {
    try {
      const studentData = await studentApi.getStudentByUserId(userId);
      setStudent(studentData);
    } catch (err) {
      setStudent(null);
    }
  }, []);

  return {
    student,
    getStudentByUserId,
  };
}
