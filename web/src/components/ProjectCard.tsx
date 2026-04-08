import { useState } from "react";
import { Copy, Check, ExternalLink, MoreVertical, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { useNavigate } from "react-router-dom";
import { useDeleteDeployment } from "@/hooks/useDeployments";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export type DeploymentStatus = "INITIALIZED" | "UPLOADING" | "IN_PROGRESS" | "FAILED" | "LIVE";

export interface Deployment {
  deploymentId: string;
  name: string;
  status: DeploymentStatus;
  apiUri: string;
  createdAt: number;
  runtime: "JAVA_17" | "NODEJS_18_X" | "PYTHON_3_12";
}

export const statusConfig: Record<string, { label: string; dotClass: string; textClass: string }> = {
  INITIALIZED: { label: "Initialized", dotClass: "bg-muted", textClass: "text-muted-foreground" },
  UPLOADING: { label: "Uploading", dotClass: "bg-status-building animate-pulse-dot", textClass: "text-status-building" },
  IN_PROGRESS: { label: "Building", dotClass: "bg-status-building animate-pulse-dot", textClass: "text-status-building" },
  FAILED: { label: "Error", dotClass: "bg-status-error", textClass: "text-status-error" },
  LIVE: { label: "Live", dotClass: "bg-status-live", textClass: "text-status-live" },
};

const runtimeLabels = {
  NODEJS_18_X: "Node.js 18",
  JAVA_17: "Java 17",
  PYTHON_3_12: "Python 3.12",
};

export function ProjectCard({ deployment }: { deployment: Deployment }) {
  const [copied, setCopied] = useState(false);
  const navigate = useNavigate();
  const status = statusConfig[deployment.status] || {
    label: deployment.status,
    dotClass: "bg-muted",
    textClass: "text-muted-foreground"
  };


  const { mutate: deleteDeployment, isPending: isDeleting } = useDeleteDeployment();

  const copyUrl = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(deployment.apiUri);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (confirm("Are you sure you want to delete this deployment?")) {
      deleteDeployment(deployment.deploymentId);
    }
  };

  return (
    <div
      onClick={() => navigate(`/deployment/${deployment.deploymentId}`)}
      className="group bg-card border border-border rounded-lg p-5 cursor-pointer transition-all duration-100 hover:shadow-md hover:border-primary/20"
    >
      <div className="flex items-start justify-between mb-3">
        <h3 className="font-medium text-card-foreground text-sm truncate pr-2">
          {deployment.name}
        </h3>
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex items-center gap-1.5 transition-all">
            <span className={cn("h-2 w-2 rounded-full", status.dotClass)} />
            <span className={cn("text-xs font-medium", status.textClass)}>
              {status.label}
            </span>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                onClick={(e) => e.stopPropagation()}
                className="p-1 rounded text-muted-foreground hover:text-foreground hover:bg-accent transition-all shrink-0"
              >
                <MoreVertical className="h-4 w-4" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem
                onClick={handleDelete}
                disabled={isDeleting}
                className="text-status-error focus:text-status-error focus:bg-status-error/10 cursor-pointer"
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Delete Deployment
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {deployment.apiUri && (
        <div className="flex items-center gap-2 mb-3 group/url">
          <code className="text-xs text-muted-foreground truncate font-mono">
            {deployment.apiUri}
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
      )}

      <div className="flex items-center justify-between text-xs text-muted-foreground">
        <span>{deployment.createdAt > 0 ? new Date(deployment.createdAt * 1000).toLocaleString() : ""}</span>
        <span className="px-1.5 py-0.5 rounded bg-accent text-accent-foreground font-medium">
          {runtimeLabels[deployment.runtime]}
        </span>
      </div>
    </div>
  );
}
