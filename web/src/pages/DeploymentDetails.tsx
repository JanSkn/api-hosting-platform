import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Copy, Check, ExternalLink, Trash2 } from "lucide-react";
import { DashboardLayout } from "@/components/DashboardLayout";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useDeployment, useDeleteDeployment } from "@/hooks/useDeployments";
import { statusConfig } from "@/components/ProjectCard";
import { fetchCloudWatchLogs } from "@/api/deployments";
import { logCache, type LogCacheEntry } from "@/utils/logCache";

const DeploymentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: deployment } = useDeployment(id ?? "");
  const { mutate: deleteDeployment, isPending: isDeleting } = useDeleteDeployment();

  const [rawLogs, setRawLogs] = useState<{ time: string; msg: string }[]>([]);
  const [visibleLogs, setVisibleLogs] = useState<{ time: string; msg: string }[]>([]);
  const [isComplete, setIsComplete] = useState(false);
  const [copied, setCopied] = useState(false);
  const logsEndRef = useRef<HTMLDivElement>(null);
  const nextTokenRef = useRef<string | undefined>(undefined);
  const [pingStatus, setPingStatus] = useState<"checking" | "operating" | "down" | null>(null);

  const functionUrl = deployment?.apiUri;
  const currentStatus = deployment?.status ? statusConfig[deployment.status] : null;
  const status = deployment?.status;

  const updateCache = (updates: Partial<LogCacheEntry>) => {
    if (!id) return;
    if (!logCache[id]) {
      logCache[id] = { rawLogs: [], visibleLogs: [], nextToken: undefined, isComplete: false };
    }
    logCache[id] = { ...logCache[id], ...updates };
  };

  // Sync state from cache if id changes / component mounts
  useEffect(() => {
    if (id) {
      const cached = logCache[id];
      setRawLogs(cached?.rawLogs ?? []);
      setVisibleLogs(cached?.visibleLogs ?? []);
      setIsComplete(cached?.isComplete ?? false);
      nextTokenRef.current = cached?.nextToken;
    }
  }, [id]);

  // Ping the API if it is LIVE
  useEffect(() => {
    if (status === "LIVE" && functionUrl) {
      setPingStatus("checking");
      fetch(functionUrl, { method: "GET" })
        .then(() => {
          setPingStatus("operating");
        })
        .catch(() => {
          setPingStatus("operating");
        });
    }
  }, [status, functionUrl]);

  // 1. Fetch CloudWatch logs (poll if in progress, single fetch if failed)
  useEffect(() => {
    if (!id || !status || status === "LIVE") return;

    let active = true;

    const poll = async () => {
      try {
        const data = await fetchCloudWatchLogs(id, nextTokenRef.current);
        if (!active) return;
        if (data.events && data.events.length > 0) {
          const newLogs = data.events.map((event) => ({
            time: new Date(event.timestamp).toLocaleTimeString(),
            msg: event.message,
          }));
          setRawLogs((prev) => {
            const existingMessages = new Set(prev.map((p) => `${p.time}_${p.msg}`));
            const filteredNewLogs = newLogs.filter((n) => !existingMessages.has(`${n.time}_${n.msg}`));
            const next = [...prev, ...filteredNewLogs];
            updateCache({ rawLogs: next });
            return next;
          });
        }
        if (data.nextToken) {
          nextTokenRef.current = data.nextToken;
          updateCache({ nextToken: data.nextToken });
        }
      } catch (err) {
        console.error("Failed to fetch CloudWatch logs", err);
      }
    };

    poll();

    if (status !== "FAILED") {
      const interval = setInterval(poll, 5000);
      return () => {
        active = false;
        clearInterval(interval);
      };
    } else {
      return () => {
        active = false;
      };
    }
  }, [id, status]);

  // 2. Print logs line by line
  useEffect(() => {
    if (rawLogs.length === 0) return;

    if (visibleLogs.length < rawLogs.length) {
      const nextIndex = visibleLogs.length;
      const timer = setTimeout(() => {
        setVisibleLogs((prev) => {
          const next = [...prev, rawLogs[nextIndex]];
          updateCache({ visibleLogs: next });
          return next;
        });
      }, 150 + Math.random() * 200);
      return () => clearTimeout(timer);
    } else if (status === "FAILED") {
      setIsComplete(true);
      updateCache({ isComplete: true });
    }
  }, [rawLogs, visibleLogs.length, status]);

  useEffect(() => {
    logsEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [visibleLogs.length]);

  const copyUrl = () => {
    if (!functionUrl) return;
    navigator.clipboard.writeText(functionUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDelete = () => {
    if (confirm("Are you sure you want to delete this deployment? This cannot be undone.")) {
      deleteDeployment(id ?? "", {
        onSuccess: () => {
          if (id) {
            delete logCache[id];
          }
          navigate("/");
        },
      });
    }
  };

  const getStatusDisplay = () => {
    if (status === "LIVE" && pingStatus) {
      if (pingStatus === "checking") {
        return {
          label: "Checking status...",
          dotClass: "bg-status-building/80 animate-pulse",
          textClass: "text-status-building",
        };
      }
      if (pingStatus === "operating") {
        return {
          label: "Operating",
          dotClass: "bg-status-live/80",
          textClass: "text-status-live",
        };
      }
      if (pingStatus === "down") {
        return {
          label: "Down",
          dotClass: "bg-status-error/80",
          textClass: "text-status-error",
        };
      }
    }
    return currentStatus
      ? {
          label: currentStatus.label,
          dotClass: currentStatus.dotClass,
          textClass: currentStatus.textClass,
        }
      : {
          label: "Unknown",
          dotClass: "bg-muted",
          textClass: "text-muted-foreground",
        };
  };

  const displayStatus = getStatusDisplay();

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

        <div className="flex items-start justify-between mb-6">
          <div>
            <h1 className="text-2xl font-semibold text-foreground tracking-tight mb-2">
              {deployment?.name ?? "Deployment Details"}
            </h1>
            <div className="flex items-center gap-2">
              <span className={cn("h-2.5 w-2.5 rounded-full", displayStatus.dotClass)} />
              <span className={cn("text-sm font-medium", displayStatus.textClass)}>
                {displayStatus.label}
              </span>
            </div>
          </div>
          <Button
            variant="outline"
            className="text-muted-foreground hover:text-status-error hover:border-status-error/40 hover:bg-status-error/5"
            onClick={handleDelete}
            disabled={isDeleting}
          >
            <Trash2 className="h-4 w-4 mr-2" />
            Delete Deployment
          </Button>
        </div>

        {status !== "LIVE" && (
          <div className="rounded-lg overflow-hidden border border-border mb-6">
            <div className="bg-terminal-bg px-4 py-2 flex items-center gap-1.5 border-b border-white/5">
              <span className="h-3 w-3 rounded-full bg-status-error/80" />
              <span className="h-3 w-3 rounded-full bg-status-building/80" />
              <span className="h-3 w-3 rounded-full bg-status-live/80" />
              <span className="ml-3 text-xs text-terminal-text/50 font-mono">Build Logs</span>
            </div>
            <div className="bg-terminal-bg p-4 h-80 overflow-y-auto font-mono text-sm">
              {visibleLogs.length === 0 ? (
                <div className="text-terminal-text/60 animate-pulse flex items-center gap-2">
                  <span>Waiting for build job to start...</span>
                </div>
              ) : (
                visibleLogs.map((log, i) => (
                  <div key={i} className="flex gap-3 mb-1">
                    <span className="text-terminal-text/40 shrink-0">[{log.time}]</span>
                    <span
                      className={cn(
                        "text-terminal-text",
                        log.msg.includes("✓") || log.msg.includes("complete")
                          ? "text-terminal-success"
                          : ""
                      )}
                    >
                      {log.msg}
                    </span>
                  </div>
                ))
              )}
              {!isComplete && visibleLogs.length > 0 && (
                <span className="text-terminal-text/40 animate-pulse">▋</span>
              )}
              <div ref={logsEndRef} />
            </div>
          </div>
        )}

        {deployment?.status === "LIVE" && functionUrl && (
          <div className="bg-card border border-status-live/20 rounded-lg p-6 text-center animate-in fade-in slide-in-from-bottom-2 duration-500">
            <div className="inline-flex items-center justify-center h-12 w-12 rounded-full bg-status-live/10 mb-4">
              <Check className="h-6 w-6 text-status-live" />
            </div>
            <h2 className="text-lg font-semibold text-foreground mb-1">Your API is Live!</h2>
            <p className="text-sm text-muted-foreground mb-4">
              Your function is deployed and ready to receive requests.
            </p>
            <div className="flex items-center justify-center gap-2 bg-accent rounded-md px-4 py-2.5 mx-auto max-w-md">
              <code className="text-sm font-mono text-foreground truncate">{functionUrl}</code>
              <button
                onClick={copyUrl}
                className="p-1 rounded hover:bg-background transition-colors shrink-0"
              >
                {copied ? (
                  <Check className="h-4 w-4 text-status-live" />
                ) : (
                  <Copy className="h-4 w-4 text-muted-foreground" />
                )}
              </button>
              <a
                href={functionUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="p-1 rounded hover:bg-background transition-colors shrink-0"
              >
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
