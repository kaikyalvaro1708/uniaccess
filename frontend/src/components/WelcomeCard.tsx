import React from "react";
import logoUniAccess from "../img/logo_uniaccess.png";
import type { PersonDetailsData } from "../types/person";

interface WelcomeCardProps {
  person: PersonDetailsData;
  onLogout: () => void;
}

const WelcomeCard: React.FC<WelcomeCardProps> = ({ person, onLogout }) => {
  const initial = person.fullName.charAt(0).toUpperCase();

  return (
    <div className="flex flex-col items-center text-center gap-6 py-8 px-6">

      {/* logo centralizada */}
      <img src={logoUniAccess} alt="UniAccess" className="h-14 object-contain" />

      {/* avatar com inicial na cor da marca */}
      <div className="w-16 h-16 rounded-full bg-orange flex items-center justify-center shrink-0 shadow-md">
        <span className="text-white font-bold text-2xl">{initial}</span>
      </div>

      {/* saudação */}
      <div>
        <p className="text-sm text-slate-400">Bem-vindo,</p>
        <h2 className="text-2xl font-bold text-slate-800 mt-0.5">
          {person.fullName}
        </h2>
        <p className="text-sm text-slate-500 mt-1">{person.email}</p>
      </div>

      {/* login badge */}
      <div className="w-full max-w-xs bg-slate-900 rounded-xl py-5 px-8">
        <p className="text-slate-400 text-xs uppercase tracking-widest mb-2">
          Seu login
        </p>
        <p className="text-white font-mono font-bold text-3xl tracking-[0.15em]">
          {person.login}
        </p>
      </div>

      {/* dados da conta */}
      <div className="w-full max-w-xs border border-slate-100 rounded-xl divide-y divide-slate-100">
        <DataRow label="Nome completo" value={person.fullName} />
        <DataRow label="CPF" value={person.document} />
        <DataRow label="E-mail" value={person.email} />
        <DataRow label="Nascimento" value={formatDate(person.dateOfBirth)} />
      </div>

      <button
        onClick={onLogout}
        className="text-sm text-slate-400 hover:text-slate-600 transition-colors cursor-pointer"
      >
        Sair
      </button>

    </div>
  );
};

const DataRow: React.FC<{ label: string; value: string }> = ({ label, value }) => (
  <div className="flex justify-between items-center px-4 py-3">
    <span className="text-xs text-slate-400">{label}</span>
    <span className="text-sm font-medium text-slate-700 truncate ml-4 max-w-[60%] text-right">
      {value}
    </span>
  </div>
);

const formatDate = (iso: string): string => {
  if (!iso) return "";
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

export default WelcomeCard;
