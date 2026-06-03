import React, { useState } from "react";
import { findPersonByEmail } from "../services/api";
import type { PersonDetailsData } from "../types/person";

interface RecoverLoginFormProps {
  onBack: () => void;
}

const RecoverLoginForm: React.FC<RecoverLoginFormProps> = ({ onBack }) => {
  const [email, setEmail]     = useState("");
  const [error, setError]     = useState("");
  const [loading, setLoading] = useState(false);
  const [person, setPerson]   = useState<PersonDetailsData | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const clean = email.trim().toLowerCase();

    if (!clean.includes("@")) {
      setError("Informe um e-mail válido");
      return;
    }

    setLoading(true);
    setError("");

    try {
      setPerson(await findPersonByEmail(clean));
    } catch {
      setError("Nenhuma conta encontrada com esse e-mail.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-6">

      <button
        onClick={onBack}
        className="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-600 transition-colors cursor-pointer w-fit"
      >
        ← Voltar ao login
      </button>

      <div>
        <h2 className="text-xl font-bold text-slate-800">Recuperar login</h2>
        <p className="text-sm text-slate-500 mt-1">
          Informe o e-mail usado no cadastro
        </p>
      </div>

      {/* resultado */}
      {person ? (
        <div className="flex flex-col items-center gap-5 py-4">
          <div className="w-full max-w-xs bg-slate-900 rounded-xl py-6 px-8 text-center">
            <p className="text-slate-400 text-xs uppercase tracking-widest mb-3">
              Seu login
            </p>
            <p className="text-white font-mono font-bold text-4xl tracking-[0.15em]">
              {person.login}
            </p>
            <p className="text-slate-500 text-xs mt-3">
              Use esse login para entrar no sistema
            </p>
          </div>

          <p className="text-sm text-slate-500 text-center">
            Olá, <span className="font-semibold text-slate-700">{person.fullName}</span>!
          </p>

          <button
            onClick={onBack}
            className="h-11 w-full max-w-xs rounded-lg bg-orange text-white font-semibold text-sm hover:bg-orange/90 transition-colors cursor-pointer"
          >
            Ir para o login →
          </button>
        </div>
      ) : (
        /* formulário */
        <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label
              htmlFor="recover-email"
              className="text-xs font-semibold text-slate-500 uppercase tracking-wider"
            >
              E-mail
            </label>
            <input
              id="recover-email"
              type="email"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setError(""); }}
              placeholder="seu@email.com"
              autoFocus
              className={[
                "h-12 px-4 rounded-lg border outline-none transition-all",
                error
                  ? "border-red-300 bg-red-50"
                  : "border-slate-200 focus:border-orange focus:ring-2 focus:ring-orange/10",
              ].join(" ")}
            />
            {error && (
              <p className="text-xs text-red-500">{error}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading || email.trim().length === 0}
            className="h-11 w-full rounded-lg bg-orange text-white font-semibold text-sm hover:bg-orange/90 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
          >
            {loading ? "Buscando…" : "Buscar login"}
          </button>
        </form>
      )}
    </div>
  );
};

export default RecoverLoginForm;
