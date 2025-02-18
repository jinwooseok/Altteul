import React from 'react';
import searchIcon from '@assets/icon/friend/Search.svg';

type SearchInputProps = {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  onSearch: () => void;
  width?: string;
  height?: string;
  onKeyDown?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
};

const SearchInput = ({
  value,
  onChange,
  width,
  height,
  placeholder,
  onSearch,
  onKeyDown,
}: SearchInputProps) => {
  return (
    <div className="relative">
      <input
        type="text"
        value={value}
        onChange={onChange}
        onKeyDown={onKeyDown}
        style={{ width, height }}
        placeholder={placeholder}
        className="pr-10 py-2 pl-4 border border-orange-500 rounded-md text-black"
      />
      <button
        onClick={onSearch}
        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-orange-500 text-xl"
      >
        <img src={searchIcon} alt="검색" />
      </button>
    </div>
  );
};

export default SearchInput;
