import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Copy, Check, ExternalLink } from "lucide-react";
import { DashboardLayout } from "@/components/DashboardLayout";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useBuildLogs, useDeployment } from "@/hooks/useDeployments";

const DeploymentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: deployment } = useDeployment(id ?? "");
  const { data: buildLogs = [] } = useBuildLogs(id ?? "");
  const [visibleLogs, setVisibleLogs] = useState<number>(0);
  const [isComplete, setIsComplete] = useState(false);
  const [copied, setCopied] = useState(false);
  const logsEndRef = useRef<HTMLDivElement>(null);

  const functionUrl = deployment?.url ?? "https://a1b2c3.lambda-url.us-east-1.on.aws";

  useEffect(() => {
    if (buildLogs.length === 0) return;
    if (visibleLogs < buildLogs.length) {
      const timer = setTimeout(() => {
        setVisibleLogs((v) => v + 1);
      }, 400 + Math.random() * 600);
      return () => clearTimeout(timer);
    } else {
      setTimeout(() => setIsComplete(true), 500);
    }
  }, [visibleLogs, buildLogs.length]);

  useEffect(() => {
    logsEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [visibleLogs]);

  const copyUrl = () => {
    navigator.clipboard.writeText(functionUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <DashboardLayout>
      <div className="p-6 lg:p-8">
        <button
          onClick={() => navigate("/")}
          className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors mb-6"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Dashboard
        </button>

        <div className="mb-6">
          <h1 className="text-2xl font-semibold text-foreground tracking-tight mb-2">
            {deployment?.name ?? "Deployment Details"}
          </h1>
          <div className="flex items-center gap-2">
            <span className={cn("h-2.5 w-2.5 rounded-full", isComplete ? "bg-status-live" : "bg-status-building animate-pulse-dot")} />
            <span className={cn("text-sm font-medium", isComplete ? "text-status-live" : "text-status-building")}>
              {isComplete ? "Live" : "Building..."}
            </span>
          </div>
        </div>

        <div className="rounded-lg overflow-hidden border border-border mb-6">
          <div className="bg-terminal-bg px-4 py-2 flex items-center gap-1.5 border-b border-white/5">
            <span className="h-3 w-3 rounded-full bg-status-error/80" />
            <span className="h-3 w-3 rounded-full bg-status-building/80" />
            <span className="h-3 w-3 rounded-full bg-status-live/80" />
            <span className="ml-3 text-xs text-terminal-text/50 font-mono">Build Logs</span>
          </div>
          <div className="bg-terminal-bg p-4 h-80 overflow-y-auto font-mono text-sm">
            {buildLogs.slice(0, visibleLogs).map((log, i) => (
              <div key={i} className="flex gap-3 mb-1">
                <span className="text-terminal-text/40 shrink-0">[{log.time}]</span>
                <span className={cn("text-terminal-text", log.msg.includes("✓") || log.msg.includes("complete") ? "text-terminal-success" : "")}>
                  {log.msg}
                </span>
              </div>
            ))}
            {!isComplete && visibleLogs > 0 && <span className="text-terminal-text/40 animate-pulse">▋</span>}
            <div ref={logsEndRef} />
          </div>
        </div>

        {isComplete && (
          <div className="bg-card border border-status-live/20 rounded-lg p-6 text-center animate-in fade-in slide-in-from-bottom-2 duration-500">
            <div className="inline-flex items-center justify-center h-12 w-12 rounded-full bg-status-live/10 mb-4">
              <Check className="h-6 w-6 text-status-live" />
            </div>
            <h2 className="text-lg font-semibold text-foreground mb-1">Your API is Live!</h2>
            <p className="text-sm text-muted-foreground mb-4">Your function is deployed and ready to receive requests.</p>
            <div className="flex items-center justify-center gap-2 bg-accent rounded-md px-4 py-2.5 mx-auto max-w-md">
              <code className="text-sm font-mono text-foreground truncate">{functionUrl}</code>
              <button onClick={copyUrl} className="p-1 rounded hover:bg-background transition-colors shrink-0">
                {copied ? <Check className="h-4 w-4 text-status-live" /> : <Copy className="h-4 w-4 text-muted-foreground" />}
              </button>
              <a href={functionUrl} target="_blank" rel="noopener noreferrer" className="p-1 rounded hover:bg-background transition-colors shrink-0">
                <ExternalLink className="h-4 w-4 text-muted-foreground" />
              </a>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default DeploymentDetails;
