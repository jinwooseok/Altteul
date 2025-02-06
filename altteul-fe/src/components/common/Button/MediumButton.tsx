// 목업에서 '게임시작' 버튼 기중

import React from "react";

type ButtonProps = {
  onClick?: () => void; // 버튼 클릭 시 동작
  type?: "button" | "submit";
  children: React.ReactNode; // 필수, 버튼에 표시될 텍스트
  backgroundColor?: string;
  fontColor?: string;
  fontSize?: string;
};

const MediumButton = ({
  onClick,
  type = "button",
  children,
  backgroundColor = "primary-orange",
  fontColor = "gray-01",
}: ButtonProps) => (
  <button
    onClick={onClick}
    type={type}
    className={`rounded-[0.5rem] cursor-pointer font-semibold px-5 py-2 bg-${backgroundColor} text-${fontColor} text-[1rem] font-semibold w-[14rem] h-[3rem]`}
  >
    {children}
  </button>
);

export default MediumButton;
