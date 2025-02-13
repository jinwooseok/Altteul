import React from 'react';

type InputProps = {
  type?: 'text' | 'password' | 'search';
  placeholder?: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  name: string;
  width?: string;
  height?: string;
  className?: string;
  buttonText?: string;
  onButtonClick?: (e: React.MouseEvent) => void;
};

const DEFAULT_INPUT_STYLE =
  'text-lg font-light text-primary-black bg-primary-white border-2 border-gray-02 rounded-lg px-4 py-2 w-full focus:ring-3 focus:ring-primary-orange focus:outline-none';
const BUTTON_STYLE =
  'ml-2 px-3 py-2 border border-primary-orange text-primary-orange bg-primary-white rounded-lg cursor-pointer hover:bg-gray-100 transition-colors border-2 '; // 호버배경 gray-100이 예뻐서 그냥 둠!

const Input = ({
  type = 'text',
  placeholder = '내용을 입력하세요',
  value,
  onChange,
  name,
  width = '23.5rem',
  height = '3rem',
  className = '',
  buttonText,
  onButtonClick,
}: InputProps) => {
  return (
    <div className="flex items-center w-full">
      <div className="flex-grow" style={{ width: `calc(100% - ${buttonText ? '6rem' : '0rem'})` }}>
        <input
          className={`${DEFAULT_INPUT_STYLE} w-full h-[${height}] ${className}`}
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          name={name}
        />
      </div>
      {buttonText && onButtonClick && (
        <button type="button" onClick={onButtonClick} className={BUTTON_STYLE}>
          {buttonText}
        </button>
      )}
    </div>
  );
};

export default Input;
