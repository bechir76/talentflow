export default function Logo({ compact = false }: { compact?: boolean }) {
  return <div className="logo"><span className="logo-mark"><span></span><span></span><span></span></span>{!compact && <strong>TalentFlow</strong>}</div>;
}
