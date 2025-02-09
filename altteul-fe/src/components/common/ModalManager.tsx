// src/components/common/Modal/ModalManager.tsx
import SignUpModal from "@components/auth/SignUpModal";
import LoginModal from "@components/auth/LoginModal";
import ResultModal from "@components/Result/ResultModal";
import useModalStore from "@stores/modalStore";
import { MODAL_TYPES } from "types/modalTypes";
import AdditionalModal from "@components/Result/AdditionalModal";
import NavigateModal from "@components/Result/NavigateModal";
import ResultDetailModal from "@components/Result/ResultDetailModal";

const ModalManager = () => {
  const { closeModal, isOpen, getModalInfo } = useModalStore();

  // 모달 정보 가져오기
  const modalInfo = getModalInfo();

  return (
    <>
      {/* 인증 관련 모달 */}
      <SignUpModal isOpen={isOpen(MODAL_TYPES.SIGNUP)} onClose={() => closeModal()} />
      <LoginModal isOpen={isOpen(MODAL_TYPES.LOGIN)} onClose={() => closeModal()} />

      {/* 게임 결과 관련 모달 */}
      <ResultModal
        isOpen={isOpen(MODAL_TYPES.RESULT)}
        onClose={() => closeModal()}
        type={modalInfo?.type}
        result={modalInfo?.result}
      />

      {/* 개인전 결과 리스트 모달 */}
      <ResultDetailModal isOpen={isOpen(MODAL_TYPES.LIST)} onClose={() => closeModal()} />

      {/* 게임 네비게이션 모달 */}
      <NavigateModal
        isOpen={isOpen(MODAL_TYPES.NAVIGATE)}
        onClose={() => closeModal()}
        type={modalInfo?.type}
      />

      {/* 공통 모달 (코드/코칭) */}
      <AdditionalModal
        isOpen={isOpen(MODAL_TYPES.COMMON)}
        onClose={() => closeModal()}
        type={modalInfo?.type}
        modalType={modalInfo?.modalType}
      />
    </>
  );
};

export default ModalManager;