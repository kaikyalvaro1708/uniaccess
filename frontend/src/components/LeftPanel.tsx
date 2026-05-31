import React from "react";
import backgroundImg from "../img/background.png";

interface LeftPanelProps {
  onLoginClick: () => void;
}

const LeftPanel: React.FC<LeftPanelProps> = ({ onLoginClick }) => {
  return (
    <div
      className="relative flex flex-col justify-between p-8 lg:p-12 min-h-[220px]"
      style={{
        backgroundImage: `url(${backgroundImg})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      {/* overlay para legibilidade */}
      <div className="absolute inset-0 bg-slate-900/62" />

      {/* conteúdo acima do overlay */}
      <div className="relative z-10 flex flex-col justify-between h-full gap-10">

        {/* wordmark */}
        <div>
          <h1 className="text-3xl font-black tracking-tight mb-2">
            <span className="text-white">Uni</span>
            <span className="text-orange">Access</span>
          </h1>
          <p className="text-white/45 text-sm tracking-wide">
            Plataforma de identidade digital
          </p>
        </div>

        {/* tagline — visível só em desktop */}
        <div className="hidden lg:flex flex-col gap-3">
          <p className="text-white/80 text-lg font-light leading-relaxed">
            Cadastre-se e receba seu{" "}
            <span className="text-orange font-semibold">login único</span>{" "}
            em segundos.
          </p>
          <p className="text-white/40 text-sm">
            Seus dados ficam armazenados com segurança e podem ser acessados a
            qualquer momento.
          </p>
        </div>

        {/* ação de login + rodapé da marca */}
        <div className="flex flex-col gap-3">
          <p className="text-white/50 text-sm">Já tem cadastro?</p>
          <button
            onClick={onLoginClick}
            className="w-full sm:w-auto px-5 py-2.5 rounded-lg border border-white/20 text-white/70 text-sm font-medium hover:border-orange hover:text-orange transition-colors cursor-pointer backdrop-blur-sm"
          >
            Entrar com seu login →
          </button>
        </div>
      </div>
    </div>
  );
};

export default LeftPanel;
