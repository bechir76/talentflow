import Icon from "@/components/common/Icon";

export default function Rating({ value }: { value: number }) {
  return <span className="rating" aria-label={`${value} out of 5 stars`}>
    {[1, 2, 3, 4, 5].map((star) => <span className={star <= value ? "filled" : ""} key={star}><Icon name="star" size={13}/></span>)}
  </span>;
}
