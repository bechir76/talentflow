import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";

const STARS = [1, 2, 3, 4, 5];

/** Star rating; read-only unless {@code onChange} is given (then it is a radio group). */
export default function Rating({ value, onChange, label }: { value: number; onChange?: (value: number) => void; label?: string }) {
  const { t } = useTranslation();
  if (!onChange) {
    return <span className="rating" aria-label={t("interviews.ratingLabel", { value })}>
      {STARS.map((star) => <span className={star <= value ? "filled" : ""} key={star}><Icon name="star" size={13}/></span>)}
    </span>;
  }
  return <span className="rating input" role="radiogroup" aria-label={label}>
    {STARS.map((star) => <button type="button" role="radio" aria-checked={star === value} aria-label={t("interviews.ratingLabel", { value: star })} className={star <= value ? "filled" : ""} key={star} onClick={() => onChange(star)}><Icon name="star" size={18}/></button>)}
  </span>;
}
