import bronze from "@assets/icon/badge/Badge_01.svg";
import silver from "@assets/icon/badge/Badge_04.svg";
import gold from "@assets/icon/badge/Badge_05.svg";
import platinum from "@assets/icon/badge/Badge_07.svg";
import dia from "@assets/icon/badge/Badge_08.svg";
import numberOne from "@assets/icon/badge/Badge_09.svg";

const badges = [
  { id: 0, name: "numberOne", src: numberOne },
  { id: 1, name: "bronze", src: bronze },
  { id: 2, name: "silver", src: silver },
  { id: 3, name: "gold", src: gold },
  { id: 4, name: "platinum", src: platinum },
  { id: 5, name: "diamond", src: dia },
];

interface BadgeFilterProps {
  tierId: number;
  selectedTier: number;
  onClick: (tierId: number) => void;
}

const BadgeFilter = ({ tierId, selectedTier, onClick }: BadgeFilterProps) => {
  const badge = badges[tierId];

  if (!badge) return null;

  return (
    <img
      key={badge.id}
      src={badge.src}
      alt={badge.name}
      className={`cursor-pointer transition-transform ${
        selectedTier === tierId ? 'scale-110' : 'hover:scale-110'
      }`}
      onClick={() => onClick(badge.id)}
    />
  );
};

export {badges, BadgeFilter}