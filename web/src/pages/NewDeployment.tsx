import { useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Upload, Github, ArrowRight, FileArchive, Plus, Trash2 } from "lucide-react";
import { DashboardLayout } from "@/components/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { cn } from "@/lib/utils";
import { getErrorMessage } from "@/lib/errors";
import { useCreateDeployment } from "@/hooks/useDeployments";
import type { DeploymentRuntime } from "@/api/deployments";
import { useToast } from "@/hooks/use-toast";
import { Loader2, HelpCircle } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Tooltip, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip";

type EnvVar = { key: string; value: string; isSecret: boolean };

const NewDeployment = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [dragOver, setDragOver] = useState(false);
  const [fileName, setFileName] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [repoUrl, setRepoUrl] = useState("");
  const [apiName, setApiName] = useState("");
  const [language, setLanguage] = useState("");
  const [runtimeVersion, setRuntimeVersion] = useState("");
  const [envVars, setEnvVars] = useState<EnvVar[]>([]);

  const versionOptions: Record<string, { value: DeploymentRuntime; label: string }[]> = {
    javascript: [
      { value: "NODEJS_22_X", label: "Node.js 22.x" },
      { value: "NODEJS_20_X", label: "Node.js 20.x" },
      { value: "NODEJS_18_X", label: "Node.js 18.x" },
    ],
    python: [
      { value: "PYTHON_3_14", label: "Python 3.14" },
      { value: "PYTHON_3_13", label: "Python 3.13" },
      { value: "PYTHON_3_12", label: "Python 3.12" },
      { value: "PYTHON_3_11", label: "Python 3.11" },
      { value: "PYTHON_3_10", label: "Python 3.10" },
    ],
    java: [
      { value: "JAVA_21", label: "Java 21 (LTS)" },
      { value: "JAVA_17", label: "Java 17 (LTS)" },
      { value: "JAVA_11", label: "Java 11 (LTS)" },
    ],
  };
  const createDeployment = useCreateDeployment();
  const { toast } = useToast();

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    if (repoUrl) return;
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file?.name.endsWith(".zip")) {
       setFileName(file.name);
       setSelectedFile(file);
       setRepoUrl("");
    }
  }, [repoUrl]);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) { 
      setFileName(file.name);
      setSelectedFile(file);
      setRepoUrl("");
    }
  };

  const clearFile = (e: React.MouseEvent) => {
    e.stopPropagation();
    setFileName(null);
    setSelectedFile(null);
  };

  const addEnvVar = () => setEnvVars([...envVars, { key: "", value: "", isSecret: false }]);
  const updateEnvVar = (index: number, field: "key" | "value" | "isSecret", val: string | boolean) => {
    const updated = [...envVars];
    // @ts-ignore
    updated[index][field] = val;
    setEnvVars(updated);
  };
  const removeEnvVar = (index: number) => setEnvVars(envVars.filter((_, i) => i !== index));

  const runtimeLabel = versionOptions[language]?.find(v => v.value === runtimeVersion)?.label ?? runtimeVersion;

  const handleDeploy = () => {
    createDeployment.mutate(
      {
        name: apiName,
        runtime: runtimeVersion as DeploymentRuntime,
        source: selectedFile || repoUrl,
        envVars,
      },
      {
        onSuccess: (deployment) => navigate(`/deployment/${deployment.id}`),
        onError: (error) =>
          toast({
            title: "Deployment failed",
            description: getErrorMessage(error),
            variant: "destructive",
          }),
      }
    );
  };

  return (
    <DashboardLayout>
      <div className="flex items-center justify-center min-h-[calc(100vh-3.5rem)]">
        <div className="w-full max-w-lg p-8">
          <h1 className="text-2xl font-semibold text-foreground tracking-tight mb-1">New Deployment</h1>
          <p className="text-sm text-muted-foreground mb-8">Deploy your API in seconds.</p>

          <div className="flex items-center gap-2 mb-8">
            {[1, 2, 3].map((s) => (
              <div key={s} className={cn("h-1 flex-1 rounded-full transition-colors", s <= step ? "bg-primary" : "bg-border")} />
            ))}
          </div>

          {step === 1 && (
            <div className="space-y-4">
              <h2 className="text-sm font-medium text-foreground">Code Source</h2>
              <div
                onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                onDragLeave={() => setDragOver(false)}
                onDrop={handleDrop}
                className={cn(
                  "border-2 border-dashed rounded-lg p-10 text-center transition-colors cursor-pointer",
                  dragOver && !repoUrl ? "border-primary bg-primary/5" : "border-border hover:border-muted-foreground/30",
                  fileName && "border-primary/50 bg-primary/5",
                  repoUrl && "cursor-not-allowed opacity-60"
                )}
                onClick={() => { if (!repoUrl) document.getElementById("file-upload")?.click(); }}
              >
                {fileName ? (
                  <div className="flex flex-col items-center gap-2 relative">
                    <FileArchive className="h-8 w-8 text-primary" />
                    <span className="text-sm font-medium text-foreground">{fileName}</span>
                    <span className="text-xs text-muted-foreground">Click to change</span>
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      onClick={clearFile}
                      className="mt-2 text-xs h-7 hover:text-destructive"
                    >
                      <Trash2 className="h-3 w-3 mr-1" /> Remove
                    </Button>
                  </div>
                ) : (
                  <div className={cn(
                    "flex flex-col items-center gap-2 transition-opacity",
                    repoUrl && "opacity-40"
                  )}>
                    <Upload className="h-8 w-8 text-muted-foreground" />
                    <span className="text-sm font-medium text-foreground">Drop your .zip file here</span>
                    <span className="text-xs text-muted-foreground">or click to browse</span>
                  </div>
                )}
                <input 
                  id="file-upload" 
                  type="file" 
                  accept=".zip" 
                  className="hidden" 
                  onChange={handleFileSelect} 
                  disabled={!!repoUrl}
                />
              </div>

              <div className="relative">
                <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-border" /></div>
                <div className="relative flex justify-center text-xs"><span className="bg-background px-2 text-muted-foreground">or</span></div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="repo-url" className="text-sm text-muted-foreground">Repository URL</Label>
                <div className="flex gap-2">
                  <div className="relative flex-1">
                    <Github className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input 
                      id="repo-url" 
                      placeholder="https://github.com/user/repo" 
                      value={repoUrl} 
                      onChange={(e) => { 
                        setRepoUrl(e.target.value); 
                        if (e.target.value) {
                          setFileName(null);
                          setSelectedFile(null);
                        }
                      }} 
                      disabled={!!fileName}
                      className="pl-9" 
                    />
                  </div>
                </div>
              </div>

              <Button onClick={() => setStep(2)} disabled={!fileName && !repoUrl} className="w-full gap-2">
                Continue <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-5">
              <h2 className="text-sm font-medium text-foreground">Configuration</h2>
              <div className="space-y-2">
                <Label htmlFor="api-name">API Name</Label>
                <Input id="api-name" placeholder="my-awesome-api" value={apiName} onChange={(e) => setApiName(e.target.value)} />
              </div>
              <div className="space-y-3">
                <Label className="text-sm font-medium text-foreground">Runtime</Label>
              <div className="space-y-2">
                <Label>Language</Label>
                <Select value={language} onValueChange={(val) => { setLanguage(val); setRuntimeVersion(""); }}>
                  <SelectTrigger><SelectValue placeholder="Select language" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="javascript">JavaScript</SelectItem>
                    <SelectItem value="python">Python</SelectItem>
                    <SelectItem value="java" disabled>
                      <span className="flex items-center gap-2">
                        Java
                        <span className="text-xs text-muted-foreground font-normal">Coming Soon</span>
                      </span>
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>
              {language && language !== "java" && (
                <div className="space-y-2">
                  <Label>Version</Label>
                  <Select value={runtimeVersion} onValueChange={setRuntimeVersion}>
                    <SelectTrigger><SelectValue placeholder="Select version" /></SelectTrigger>
                    <SelectContent>
                      {versionOptions[language].map((v) => (
                        <SelectItem key={v.value} value={v.value}>{v.label}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Label>Environment Variables</Label>
                  <Button variant="ghost" size="sm" onClick={addEnvVar} className="gap-1 h-7 text-xs"><Plus className="h-3 w-3" />Add</Button>
                </div>
                {envVars.length > 0 && (
                  <div className="border border-border rounded-lg overflow-hidden">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead className="h-9 text-xs">Key</TableHead>
                          <TableHead className="h-9 text-xs">Value</TableHead>
                          <TableHead className="h-9 text-xs w-16 text-center">
                            <span className="flex items-center justify-center gap-1">
                              Secret
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <HelpCircle className="h-3.5 w-2.5 text-muted-foreground cursor-help" />
                                </TooltipTrigger>
                                <TooltipContent>
                                  <p className="text-xs">Securely encrypts and hides this variable in deployment logs.</p>
                                </TooltipContent>
                              </Tooltip>
                            </span>
                          </TableHead>
                          <TableHead className="h-9 w-10" />
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {envVars.map((env, i) => (
                          <TableRow key={i}>
                            <TableCell className="p-1.5"><Input placeholder="KEY" value={env.key} onChange={(e) => updateEnvVar(i, "key", e.target.value)} className="h-8 text-xs font-mono" /></TableCell>
                            <TableCell className="p-1.5"><Input placeholder="value" value={env.value} onChange={(e) => updateEnvVar(i, "value", e.target.value)} className="h-8 text-xs font-mono" /></TableCell>
                            <TableCell className="p-1.5 text-center">
                              <span className="flex justify-center">
                                <Switch
                                  checked={env.isSecret}
                                  onCheckedChange={(checked) => updateEnvVar(i, "isSecret", checked)}
                                />
                              </span>
                            </TableCell>
                            <TableCell className="p-1.5"><Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground hover:text-destructive" onClick={() => removeEnvVar(i)}><Trash2 className="h-3.5 w-3.5" /></Button></TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
                {envVars.length === 0 && <p className="text-xs text-muted-foreground">No environment variables added yet.</p>}
              </div>

              <div className="flex gap-3 pt-2">
                <Button variant="outline" onClick={() => setStep(1)} className="flex-1">Back</Button>
                <Button onClick={() => setStep(3)} disabled={!apiName || !language || !runtimeVersion} className="flex-1 gap-2">Continue <ArrowRight className="h-4 w-4" /></Button>
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-5 text-center">
              <h2 className="text-sm font-medium text-foreground">Ready to Deploy</h2>
              <div className="bg-card border border-border rounded-lg p-5 text-left space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Source</span>
                  <span className="font-mono text-foreground text-xs">{fileName || repoUrl}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">API Name</span>
                  <span className="font-medium text-foreground">{apiName}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Runtime</span>
                  <span className="text-foreground">{runtimeLabel}</span>
                </div>
                {envVars.length > 0 && (
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Env Variables</span>
                    <span className="text-foreground">
                      {envVars.length} defined ({envVars.filter((e) => e.isSecret).length} secret)
                    </span>
                  </div>
                )}
              </div>
              <div className="flex gap-3">
                <Button variant="outline" onClick={() => setStep(2)} className="flex-1">Back</Button>
                <Button onClick={handleDeploy} className="flex-1 gap-2" size="lg" disabled={createDeployment.isPending}>
                  {createDeployment.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <>Deploy API <ArrowRight className="h-4 w-4" /></>}
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default NewDeployment;
