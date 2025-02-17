import GameGnb from '@components/Nav/GameGnb';
import MainGnb from '@components/Nav/MainGnb';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import ModalManager from '@utils/ModalManager';
import { useEffect } from 'react';
import { useSocketStore } from '@stores/socketStore';
import { inviteResponse } from '@utils/Api/matchApi';
import socketResponseMessage from 'types/socketResponseMessage';
import Button from '@components/Common/Button/Button';
import { MODAL_TYPES } from 'types/modalTypes';
import chatmodalimg from '@assets/icon/chatmodal.svg';

// 임시 친구모달 버튼
import useModalStore from '@stores/modalStore';
import { useMatchStore } from '@stores/matchStore';
import useFriendChatStore from '@stores/friendChatStore';

const App = () => {
  const location = useLocation();
  const isGamePage = location.pathname.startsWith('/game');
  const socket = useSocketStore();
  const navigate = useNavigate();
  const matchStore = useMatchStore();
  const { openModal } = useModalStore();
  const fcStore = useFriendChatStore();

  //로그인 시 소켓 연결 유지
  useEffect(() => {
    const wasConnected = sessionStorage.getItem('wsConnected') === 'true';
    if (wasConnected && !!sessionStorage.getItem('token')) {
      socket.connect(); //로그인 성공시 소켓 연결
    }
  }, []);

  // 로그인 & 소켓 연결 성공 시 친구관련 구독 신청
  //TODO: App.tsx기 때문에 페이지에 따른 구독신청과 구독취소 관리 필요함(안하면 문제 풀다가 초대 요청 받을 수 있음)
  useEffect(() => {
    const userId = sessionStorage.getItem('userId');
    if (socket.connected && userId) {
      socket.subscribe(`/sub/invite/${userId}`, handleMessage); //게임 초대 구독
      socket.subscribe(`/sub/notification/${userId}`, handleMessage); //친구 신청 구독
      socket.subscribe(`/sub/friend/update/${userId}`, handleMessage); //친구 수락/거절 구독
    }
  }, [socket.connected]);

  //전체 소켓 응답 메세지 핸들러
  const handleMessage = async (message: socketResponseMessage) => {
    const { type, data } = message;
    console.log(message)
    if (type === 'INVITE_REQUEST_RECEIVED') {
      //TODO: confirm 말고 다른 방식의 요청 수락/거절 형식 필요
      if (confirm(`${data.nickname || '알 수 없음'}님이 팀전에 초대하셨습니다.`)) {
        try {
          //TODO: 응답(res.status)에 따른 처리 필요
          const res = await inviteResponse(data.nickname, data.roomId, true); //친구 초대 수락 api
          matchStore.setMatchData(res.data); //초대받은 방으로 이동 후 쓰일 data setting
          navigate(`/match/team/composition`); //팀전 대기방으로 이동
        } catch (error) {
          console.error('초대 수락 중 오류 발생:', error);
        }
      }
    }else{
      console.log(message)
    }

    if (type === 'SEND_REQUEST') {
      //친구 신청 받았을 때 데이터 FriendChatStore에 저장
      // fcStore.setFriendRequests(data.friendRequests)
      //TODO: 친구 신청이 왔다는 알림(?)
    }
  };

  const hideNavigation = [
    '/match/team/composition',
    '/match/team/search',
    '/match/team/final',
    '/match/single/search',
    '/match/single/final',
    ,
  ].includes(location.pathname);

  const showFriendChatModalButton = [
    '/', '/rank', '/match/select', '/match/single/search', '/match/team/composition'
  ].includes(location.pathname);

  const transparentNavigation = ['/match/select', '/rank', '/users/:userId'].includes(
    location.pathname
  );

  return (
    <>
      <div className="min-h-screen">
        {!hideNavigation && (isGamePage ? <GameGnb /> : <MainGnb />)}
        <main
          className={`${transparentNavigation ? '' : 'mt-[3.5rem]'} bg-primary-black h-[calc(100vh-3.5rem)]`}
        >
          <Outlet />
          {/* // 임시버튼 - 친구 */}
          {showFriendChatModalButton && <button
            onClick={() => openModal(MODAL_TYPES.MAIN)}
            className="fixed bottom-5 right-5 z-50"
          >
            <img src={chatmodalimg} alt="임시채팅모달" className="w-12 h-12 object-contain" />
          </button>}
        </main>
        <ModalManager />
      </div>
    </>
  );
};

export default App;
