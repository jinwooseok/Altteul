// 크기 조정 가능!, 목업에서 네브바 로그인 버튼 기준임

import React from "react";

type ButtonProps = {
  onClick?: () => void; // 버튼 클릭 시 동작
  type?: "button" | "submit";
  children: React.ReactNode; // 필수, 버튼에 표시될 텍스트
  backgroundColor?: string;
  fontColor?: string;
  fontSize?: string;
  width?: string;
  height?: string;
};

const SmallButton = ({
  onClick,
  type = "button",
  children,
  backgroundColor = "primary-orange",
  fontColor = "gray-01",
  width = "4.8125rem",
  height = "2rem",
  fontSize = "1.125rem",
}: ButtonProps) => (
  <button
    onClick={onClick}
    type={type}
    className={`rounded-[0.5rem] cursor-pointer font-semibold ml-2 px-3 py-1 bg-${backgroundColor} text-${fontColor} text-${fontSize} font-semibold w-[${width}] h-[${height}]`}
  >
    {children}
  </button>
);

export default SmallButton;
