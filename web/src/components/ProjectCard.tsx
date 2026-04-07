import { useState } from "react";
import { Copy, Check, ExternalLink } from "lucide-react";
import { cn } from "@/lib/utils";
import { useNavigate } from "react-router-dom";

export type DeploymentStatus = "LIVE" | "IN_PROGRESS" | "FAILED" | "UPLOADING"; // defined in backend

export interface Deployment {
  id: string;
  name: string;
  status: DeploymentStatus;
  url: string;
  lastDeployed: string;
  runtime: "node" | "python";
}

export const statusConfig: Record<string, { label: string; dotClass: string; textClass: string }> = {
  LIVE: { label: "Live", dotClass: "bg-status-live", textClass: "text-status-live" },
  IN_PROGRESS: { label: "Building", dotClass: "bg-status-building animate-pulse-dot", textClass: "text-status-building" },
  FAILED: { label: "Error", dotClass: "bg-status-error", textClass: "text-status-error" },
  UPLOADING: { label: "Uploading", dotClass: "bg-status-building animate-pulse-dot", textClass: "text-status-building" },
};


const runtimeLabels = {
  node: "Node.js",
  python: "Python",
};

export function ProjectCard({ deployment }: { deployment: Deployment }) {
  const [copied, setCopied] = useState(false);
  const navigate = useNavigate();
  const status = statusConfig[deployment.status] || {
    label: deployment.status,
    dotClass: "bg-muted",
    textClass: "text-muted-foreground"
  };


  const copyUrl = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(deployment.url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div
      onClick={() => navigate(`/deployment/${deployment.id}`)}
      className="group bg-card border border-border rounded-lg p-5 cursor-pointer transition-all duration-100 hover:shadow-md hover:border-primary/20"
    >
      <div className="flex items-start justify-between mb-3">
        <h3 className="font-medium text-card-foreground text-sm truncate pr-2">
          {deployment.name}
        </h3>
        <div className="flex items-center gap-1.5 shrink-0">
          <span className={cn("h-2 w-2 rounded-full", status.dotClass)} />
          <span className={cn("text-xs font-medium", status.textClass)}>
            {status.label}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-2 mb-3 group/url">
        <code className="text-xs text-muted-foreground truncate font-mono">
          {deployment.url}
        </code>
        <button
          onClick={copyUrl}
          className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-accent transition-all shrink-0"
        >
          {copied ? (
            <Check className="h-3 w-3 text-status-live" />
          ) : (
            <Copy className="h-3 w-3 text-muted-foreground" />
          )}
        </button>
      </div>

      <div className="flex items-center justify-between text-xs text-muted-foreground">
        <span>{deployment.lastDeployed}</span>
        <span className="px-1.5 py-0.5 rounded bg-accent text-accent-foreground font-medium">
          {runtimeLabels[deployment.runtime]}
        </span>
      </div>
    </div>
  );
}
