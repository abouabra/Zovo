import { format, isToday, isYesterday } from "date-fns";

const DateSeparator = ({ date }: { date: string }) => {
  const dateObj = new Date(date);
  let label = format(dateObj, "dd/MM/yyyy");

  if (isToday(dateObj)) label = "Today";
  else if (isYesterday(dateObj)) label = "Yesterday";

  return (
    <div className="flex justify-center my-2">
      <div className="text-body2 rounded-full bg-input-bg py-2 px-4">
        {label}
      </div>
    </div>
  );
};

export default DateSeparator;
