import React from "react";

interface FormFieldProps {
  label: string;
  name: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  error?: string;
  hint?: string;
  type?: string;
  placeholder?: string;
  disabled?: boolean;
  maxLength?: number;
  max?: string;
  required?: boolean;
}

const FormField: React.FC<FormFieldProps> = ({
  label,
  name,
  value,
  onChange,
  onBlur,
  error,
  hint,
  type = "text",
  placeholder,
  disabled = false,
  maxLength,
  max,
  required = true,
}) => {
  return (
    <div className="flex flex-col gap-1">
      <label
        htmlFor={name}
        className="text-xs font-semibold text-slate-500 uppercase tracking-wider"
      >
        {label}
        {!required && (
          <span className="ml-1 font-normal normal-case text-slate-400">
            (opcional)
          </span>
        )}
      </label>
      <input
        id={name}
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onBlur={onBlur}
        placeholder={placeholder}
        disabled={disabled}
        maxLength={maxLength}
        max={max}
        className={[
          "h-10 px-3 rounded-lg border text-sm outline-none transition-all",
          "placeholder:text-slate-300 w-full",
          error
            ? "border-red-300 bg-red-50 focus:border-red-400"
            : "border-slate-200 bg-white focus:border-orange focus:ring-2 focus:ring-orange/10",
          disabled && "bg-slate-50 text-slate-400 cursor-not-allowed",
        ]
          .filter(Boolean)
          .join(" ")}
      />
      {error
        ? <p className="text-xs text-red-500">{error}</p>
        : hint && <p className="text-xs text-slate-400">{hint}</p>
      }
    </div>
  );
};

export default FormField;
