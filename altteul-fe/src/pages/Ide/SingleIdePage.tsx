import { useEffect, useState } from 'react';
import useGameWebSocket from '@hooks/useGameWebSocket';
import useGameStore from '@stores/useGameStore';
import CodeEditor from '@components/Ide/CodeEditor';
import Terminal from '@components/Ide/Terminal';
import IdeFooter from '@components/Ide/IdeFooter';
import ProblemInfo from '@components/Ide/ProblemInfo';
import SideProblemModal from '@components/Ide/SideProblemModal';

const MAX_REQUESTS = 5;

const SingleIdePage = () => {
  const { gameId, roomId, users, problem } = useGameStore();
  const {
    sideProblem,
    sideProblemResult,
    codeResult,
    opponentCodeResult,
    requestSideProblem,
    submitSideProblemAnswer,
    submitCode,
  } = useGameWebSocket(gameId, roomId);

  const [code, setCode] = useState('');
  const [language, setLanguage] = useState<'python' | 'java'>('python');
  const [showModal, setShowModal] = useState(false);
  const [requestCount, setRequestCount] = useState(0)

  console.log(sideProblemResult);

  // ✅ 5분마다 자동으로 사이드 문제 요청
  // TODO: 서버 버그 해결 시 주석 해제
  useEffect(() => {
    if (requestCount >= MAX_REQUESTS) return; // ✅ 5번 넘으면 실행 중단

    const interval = setInterval(() => {
      if (requestCount < MAX_REQUESTS) {
        requestSideProblem();
        setRequestCount(prev => prev + 1); // ✅ 요청 횟수 증가
      } else {
        clearInterval(interval); // ✅ 5번 요청하면 멈춤
      }
    }, 5 * 60 * 1000); // 5분

    return () => clearInterval(interval);
  }, [requestCount, requestSideProblem]);

  // ✅ 사이드 문제가 도착하면 모달 띄우기
  useEffect(() => {
    if (sideProblem) {
      setShowModal(true);
    }
  }, [sideProblem]);

  return (
    <div className="flex h-screen bg-primary-black border-t border-gray-04">
      <div className="min-w-[30rem] border-r border-gray-04">
        <ProblemInfo />
      </div>

      <div className="max-w-[65rem] flex-[46rem] border-r border-gray-04">
        <CodeEditor code={code} setCode={setCode} language={language} setLanguage={setLanguage} />
        <Terminal output={codeResult ? JSON.stringify(codeResult, null, 2) : ''} />
        <IdeFooter onExecute={() => submitCode(problem.problemId, language, code)} />
      </div>
      {/* ✅ 사이드 문제 모달 */}
      {showModal && sideProblem && (
        <SideProblemModal
          gameId={gameId}
          roomId={roomId}
          problem={sideProblem?.data}
          onClose={() => setShowModal(false)}
        />
      )}
    </div>
  );
};

export default SingleIdePage;
