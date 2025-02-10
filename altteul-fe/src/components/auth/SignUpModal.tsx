// ** 회원가입 모달 컴포넌트 **
// 추가할 것 - 아이디, 닉네임 중복 확인

import { useState } from "react";

import Input from "@components/common/Input";
import Modal from "@components/common/Modal";
import Button from "@components/common/Button/Button";
import Dropdown from "@components/common/Dropdown";

import { checkUsername, registerUser } from "@utils/api/auth";
import {
  validateSignUpForm,
  SignUpFormData,
  ValidationErrors,
} from "@utils/validation";

// 회원가입 모달에 필요한 props
interface SignUpProps {
  isOpen: boolean;
  onClose: () => void;
}

const SignUpModal = ({ isOpen, onClose }: SignUpProps) => {
  // 언어 매핑
  const languageOptions = [
    { id: 1, value: "PY", label: "Python" },
    { id: 2, value: "JV", label: "Java" },
  ];

  // 입력 폼 상태
  const [form, setForm] = useState<SignUpFormData>({
    username: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    mainLang: "PY",
    profileImg: null,
  });

  // 에러메시지 상태 추가
  const [errors, setErrors] = useState<ValidationErrors>({
    username: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    mainLang: "",
    profileImg: "",
  });

  // API 요청 시 발생하는 에러 메시지
  const [apiError, setApiError] = useState("");
  // 로딩 상태 관리 - 추후 로딩 스피너 사용
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 아이디 중복 확인 - 추후 수정
  // const [isUsernameTaken, setIsUsernameTaken] = useState(false);
  // const [isCheckingUsername, setIsCheckingUsername] = useState(false);

  // 입력값 변경 핸들러 (input 필드 값 바뀔 때 실행)
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // 드롭다운 언어 선택
  const handleSelectChange = (selected: string) => {
    setForm({ ...form, mainLang: selected });
  };

  //이미지 파일 업로드 처리
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.length) {
      setForm({ ...form, profileImg: e.target.files[0] });
    }
  };

  // 폼 유효성 검사
  const validateForm = () => {
    const validationResult = validateSignUpForm(form);
    setErrors(validationResult.errors);

    // 비밀번호 확인
    if (form.password !== form.confirmPassword) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        confirmPassword: "비밀번호가 일치하지 않습니다.",
      }));
      return false;
    }

    return validationResult.isValid;
  };

  // 아이디 중복 확인 핸들러
  // const handleCheckUsername = async () => {
  //   setIsCheckingUsername(true);
  //   setIsUsernameTaken(false);

  //   try {
  //     const response = await checkUsername(form.username);
  //     if (response.isTaken) {
  //       setIsUsernameTaken(true);
  //       setErrors((prevErrors) => ({
  //         ...prevErrors,
  //         username: "이미 사용 중인 아이디입니다.",
  //       }));
  //     } else {
  //       setIsUsernameTaken(false);
  //       setErrors((prevErrors) => ({
  //         ...prevErrors,
  //         username: "",
  //       }));
  //     }
  //   } catch (error) {
  //     console.error("아이디 중복 확인 실패:", error);
  //     setApiError("아이디 중복 확인에 실패했습니다.");
  //   } finally {
  //     setIsCheckingUsername(false);
  //   }
  // };

  // 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // 유효성 검사 실패 시 중단
    if (!validateForm()) return;

    // 폼 입력값 확인용 console.log
    console.log("입력값:", form);

    // 폼 데이터 생성
    const formData = new FormData();

    const requestData = {
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      mainLang: form.mainLang,
    };

    formData.append("request", JSON.stringify(requestData));

    // profileImg는 파일로 추가
    if (form.profileImg) {
      formData.append("profileImg", form.profileImg);
    } else {
      formData.append("profileImg", "");
    }

    try {
      setIsSubmitting(true); // 제출 중 상태 활성화

      // API 호출
      const response = await registerUser(formData); // api.ts에서 정의한 함수 사용

      // 성공처리 - 200번대
      if (response.status >= 200 && response.status < 300) {
        console.log("회원가입 성공");
        onClose();

        // 폼 리셋
        setForm({
          username: "",
          password: "",
          confirmPassword: "",
          nickname: "",
          mainLang: "PY",
          profileImg: null,
        });

        // 에러 메시지도 초기화
        setErrors({
          username: "",
          password: "",
          confirmPassword: "",
          nickname: "",
          mainLang: "",
          profileImg: "",
        });
      } else {
        setApiError(response.message || "잘못된 응답입니다.");
      }
    } catch (error: unknown) {

      if (error instanceof Error) {
        console.error("회원가입 중 오류 발생 : ", error);
        setApiError(
          error.message || "서버와 연결 할 수 없습니다. 다시 시도하세요."
        );
      }

    } finally {
      setIsSubmitting(false); // 로딩 끝
    }
  };

  // 비밀번호 일치여부 확인
  const checkPasswordMatch = () => {
    if (
      form.password &&
      form.confirmPassword &&
      form.password !== form.confirmPassword
    ) {
      return "비밀번호가 일치하지 않습니다.";
    } else if (
      form.password &&
      form.confirmPassword &&
      form.password === form.confirmPassword
    ) {
      return "비밀번호가 일치합니다.";
    }
    return "";
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="알뜰 회원가입"
      height="auto"
      className="bg-primary-white"
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-5 mt-6">
        <Input
          name="username"
          type="text"
          placeholder="아이디를 입력해 주세요."
          onChange={handleChange}
          value={form.username}
          className="w-[22rem] "
        />
        {errors.username && (
          <p className="text-primary-orange text-sm">{errors.username}</p>
        )}
        {apiError && <p className="text-primary-orange text-sm">{apiError}</p>}
        <Button children="중복확인(현재기능x)" className="h-[3rem]" />
        {/*         
        <Button
          className={`w-full h-[2.8rem] ${
            isCheckingUsername
              ? "bg-gray-03 cursor-not-allowed"
              : "bg-primary-orange"
          }`}
          onClick={handleCheckUsername}
          disabled={isCheckingUsername}
        >
          {isCheckingUsername ? "확인중..." : "아이디 중복 확인"}
        </Button> */}

        <Input
          name="password"
          type="password"
          placeholder="비밀번호를 입력해 주세요."
          onChange={handleChange}
          value={form.password}
        />
        {errors.password && (
          <p className="text-primary-orange text-sm">{errors.password}</p>
        )}
        <Input
          name="confirmPassword"
          type="password"
          placeholder="비밀번호 확인"
          onChange={handleChange}
          value={form.confirmPassword}
        />
        {errors.confirmPassword && (
          <p className="text-primary-orange text-sm">
            {errors.confirmPassword}
          </p>
        )}
        {checkPasswordMatch() && (
          <p
            className={`text-sm ${
              form.password === form.confirmPassword
                ? "text-primary-orange font-semibold"
                : "text-gray-03 font-semibold"
            }`}
          >
            {checkPasswordMatch()}
          </p>
        )}

        <Input
          name="nickname"
          type="text"
          placeholder="닉네임을 입력해 주세요"
          onChange={handleChange}
          value={form.nickname}
        />
        {errors.nickname && (
          <p className="text-primary-orange text-sm">{errors.nickname}</p>
        )}
        <Dropdown
          options={languageOptions}
          value={form.mainLang}
          onChange={handleSelectChange}
          className="bg-primary-white border rounded-xl"
        />
        {errors.mainLang && (
          <p className="text-primary-orange text-sm">{errors.mainLang}</p>
        )}
        <div>
          <input
            type="file"
            name="profileImg"
            onChange={handleFileChange}
            accept="image/png, image/jpg, image/jpeg"
          />
          {errors.profileImg && <p className="error">{errors.profileImg}</p>}
        </div>
        {/* 제출중일때 버튼 비활성화 (추후 로딩스피너 추가할 때 수정예정) */}
        <Button
          type="submit"
          className="h-[3rem]"
        >
          {isSubmitting ? "처리중..." : "가입하기"}
        </Button>
      </form>
    </Modal>
  );
};

export default SignUpModal;
