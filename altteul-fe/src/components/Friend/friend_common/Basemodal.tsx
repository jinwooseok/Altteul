// components/friend/friend_common/Basemodal.tsx
import React from 'react';
import ModalHeader from '@components/Friend/ModalHeader';

type BaseModalProps = {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  showBackButton?: boolean;
  onBack?: () => void;
};

const BaseModal = ({ isOpen, onClose, children, showBackButton, onBack }: BaseModalProps) => {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 flex items-center justify-center bg-primary-black bg-opacity-50"
      onClick={onClose}
    >
      <div
        className="bg-gray-06 border-2 border-primary-orange rounded-lg w-[90vw] max-w-md h-[90vh] max-h-[80vh] p-4 shadow-lg relative flex flex-col"
        onClick={e => e.stopPropagation()}
      >
        <ModalHeader showBackButton={showBackButton} onBack={onBack} onClose={onClose} />
        {children}
      </div>
    </div>
  );
};

export default BaseModal;
