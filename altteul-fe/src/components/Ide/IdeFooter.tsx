import SmallButton from '@components/Common/Button/SmallButton ';
import useCodeExecutionStore from '@stores/useCodeExecutionStore';

interface IdeFooterProps {
  onExecute: () => void;
}

const IdeFooter: React.FC<IdeFooterProps> = ({ onExecute }) => {
  const handleSubmitCode = () => {
    // 코드 제출 axios
  };

  return (
    <div className='flex justify-end items-center p-2 bg-primary-black border-t border-gray-04'>
      <SmallButton onClick={onExecute} children='코드 실행' backgroundColor='gray-04' fontSize='md' />
      <SmallButton onClick={handleSubmitCode} children='코드 제출' fontSize='md' type='submit' />
    </div>
  );
};

export default IdeFooter;
