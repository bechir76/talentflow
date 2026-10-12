type AvatarProps = { initials: string; color?: string; small?: boolean };

export default function Avatar({ initials, color = "blue", small = false }: AvatarProps) {
  return <span className={`avatar avatar-${color} ${small ? "avatar-small" : ""}`}>{initials}</span>;
}
