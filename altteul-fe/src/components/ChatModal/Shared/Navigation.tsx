// 하단 네비게이션 바
// src/components/Modal/Chat/shared/Navigation.tsx
import Friend_list from '@assets/icon/friend/Friend_list.svg';
import Chat_bubble from '@assets/icon/friend/Chat_bubble.svg';
import Notifications from '@assets/icon/friend/Notifications.svg';
import s_friend_list from '@assets/icon/friend/s_friend_list.svg';
import s_Chat_bubble from '@assets/icon/friend/s_Chat_bubble.svg';
import s_Notifications from '@assets/icon/friend/s_Notifications.svg';

type NavigationProps = {
  currentTab: 'friends' | 'chats' | 'notifications';
  onTabChange: (tab: 'friends' | 'chats' | 'notifications') => void;
};

const Navigation = ({ currentTab, onTabChange }: NavigationProps) => {
  return (
    <div className="mt-4 flex justify-around border-t border-gray-700 pt-3">
      <button
        onClick={() => onTabChange('friends')}
        className="relative flex flex-col items-center p-1 hover:scale-110 rounded-lg"
      >
        <img
          src={currentTab === 'friends' ? s_friend_list : Friend_list}
          alt="친구목록"
          className="w-10 h-10"
        />
      </button>

      <button
        onClick={() => onTabChange('chats')}
        className="relative flex flex-col items-center px-4 py-2 hover:scale-110 rounded-lg"
      >
        <img
          src={currentTab === 'chats' ? s_Chat_bubble : Chat_bubble}
          alt="채팅목록"
          className="w-9 h-9"
        />
      </button>

      <button
        onClick={() => onTabChange('notifications')}
        className="relative flex flex-col items-center px-4 py-2 hover:scale-110 rounded-lg"
      >
        <img
          src={currentTab === 'notifications' ? s_Notifications : Notifications}
          alt="알림목록"
          className="w-9 h-9"
        />
      </button>
    </div>
  );
};

export default Navigation;
