import React from "react";
import logoUniAccess from "../img/logo_uniaccess.png";
import type { PersonDetailsData } from "../types/person";

interface SuccessCardProps {
  person: PersonDetailsData;
  onReset: () => void;
}

const SuccessCard: React.FC<SuccessCardProps> = ({ person, onReset }) => {
  return (
    <div className="flex flex-col items-center text-center gap-6 py-8 px-6">
      {/* ícone de sucesso */}
      <div className="w-14 h-14 rounded-full bg-green-50 border-2 border-green-200 flex items-center justify-center">
        <svg
          className="w-6 h-6 text-green-500"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2.5}
            d="M5 13l4 4L19 7"
          />
        </svg>
      </div>

      <div>
        <h2 className="text-xl font-bold text-slate-800">
          Cadastro realizado!
        </h2>
        <p className="text-sm text-slate-500 mt-1">
          Seu login foi gerado automaticamente.
        </p>
      </div>

      {/* login em destaque */}
      <div className="w-full max-w-xs bg-slate-900 rounded-xl py-6 px-8">
        <p className="text-slate-400 text-xs uppercase tracking-widest mb-3">
          Login gerado
        </p>
        <p className="text-white font-mono font-bold text-4xl tracking-[0.15em]">
          {person.login}
        </p>
        <p className="text-slate-500 text-xs mt-3">
          Use esse login para entrar no sistema
        </p>
      </div>

      {/* resumo dos dados */}
      <div className="w-full max-w-xs border border-slate-100 rounded-xl divide-y divide-slate-100">
        <DataRow label="Nome" value={person.fullName} />
        <DataRow label="CPF" value={person.document} />
        <DataRow label="E-mail" value={person.email} />
        <DataRow label="Nascimento" value={formatDate(person.dateOfBirth)} />
      </div>

      <button
        onClick={onReset}
        className="text-sm text-orange font-semibold hover:underline cursor-pointer"
      >
        + Cadastrar outra pessoa
      </button>

      {/* logo no rodapé — opção 3 */}
      <img
        src={logoUniAccess}
        alt="UniAccess"
        className="h-7 object-contain opacity-60"
      />
    </div>
  );
};

const DataRow: React.FC<{ label: string; value: string }> = ({
  label,
  value,
}) => (
  <div className="flex justify-between items-center px-4 py-3">
    <span className="text-xs text-slate-400">{label}</span>
    <span className="text-sm font-medium text-slate-700">{value}</span>
  </div>
);

const formatDate = (iso: string): string => {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

export default SuccessCard;
