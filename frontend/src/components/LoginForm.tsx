import React, { useState } from "react";
import { findPersonByLogin } from "../services/api";
import type { PersonDetailsData } from "../types/person";

interface LoginFormProps {
  onSuccess: (person: PersonDetailsData) => void;
  onBack: () => void;
}

const LoginForm: React.FC<LoginFormProps> = ({ onSuccess, onBack }) => {
  const [login, setLogin] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const clean = login.trim().toLowerCase();

    if (!/^[a-z]{7}$/.test(clean)) {
      setError("O login deve ter exatamente 7 letras minúsculas");
      return;
    }

    setLoading(true);
    setError("");

    try {
      onSuccess(await findPersonByLogin(clean));
    } catch {
      setError("Login não encontrado. Verifique e tente novamente.");
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
        ← Voltar
      </button>

      <div>
        <h2 className="text-xl font-bold text-slate-800">Entrar</h2>
        <p className="text-sm text-slate-500 mt-1">
          Use o login gerado no seu cadastro
        </p>
      </div>

      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <label
            htmlFor="login-input"
            className="text-xs font-semibold text-slate-500 uppercase tracking-wider"
          >
            Login
          </label>
          <input
            id="login-input"
            type="text"
            value={login}
            onChange={(e) => {
              setLogin(e.target.value.toLowerCase());
              setError("");
            }}
            placeholder="ex: mariasi"
            maxLength={7}
            autoFocus
            spellCheck={false}
            className={[
              "h-12 px-4 rounded-lg border text-center text-xl font-mono tracking-[0.25em] outline-none transition-all",
              error
                ? "border-red-300 bg-red-50"
                : "border-slate-200 focus:border-orange focus:ring-2 focus:ring-orange/10",
            ].join(" ")}
          />
          {error ? (
            <p className="text-xs text-red-500 text-center">{error}</p>
          ) : (
            <p className="text-xs text-slate-400 text-center">
              7 letras minúsculas
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={loading || login.trim().length === 0}
          className="h-11 w-full rounded-lg bg-orange text-white font-semibold text-sm hover:bg-orange/90 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
        >
          {loading ? "Verificando…" : "Entrar"}
        </button>
      </form>

      <p className="text-xs text-slate-400 text-center">
        Ainda não tem cadastro?{" "}
        <button
          onClick={onBack}
          className="text-orange font-semibold hover:underline cursor-pointer"
        >
          Cadastrar agora
        </button>
      </p>
    </div>
  );
};

export default LoginForm;
