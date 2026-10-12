import { useState } from "react";
import { useTranslation } from "react-i18next";

type SkillsInputProps = {
  value: string[];
  onChange: (skills: string[]) => void;
  onBlur: () => void;
  invalid?: boolean;
  id?: string;
};

/** Tag input: type a skill and press Enter or comma to add it. */
export default function SkillsInput({ value, onChange, onBlur, invalid, id }: SkillsInputProps) {
  const { t } = useTranslation();
  const [draft, setDraft] = useState("");

  const add = () => {
    const skill = draft.trim();
    if (skill && !value.some((existing) => existing.toLowerCase() === skill.toLowerCase())) {
      onChange([...value, skill]);
    }
    setDraft("");
  };

  return <div className={`tag-input ${invalid ? "invalid" : ""}`}>
    {value.map((skill) => <span key={skill}>{skill}<button type="button" aria-label={t("jobs.form.removeSkill", { skill })} onClick={() => onChange(value.filter((item) => item !== skill))}>×</button></span>)}
    <input
      id={id}
      value={draft}
      onChange={(event) => setDraft(event.target.value)}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === ",") {
          event.preventDefault();
          add();
        } else if (event.key === "Backspace" && !draft && value.length > 0) {
          onChange(value.slice(0, -1));
        }
      }}
      onBlur={() => { add(); onBlur(); }}
      placeholder={t("jobs.form.skillsPlaceholder")}
    />
  </div>;
}
