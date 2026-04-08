import { Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { DashboardLayout } from "@/components/DashboardLayout";
import { ProjectCard } from "@/components/ProjectCard";
import { Button } from "@/components/ui/button";
import { useDeployments } from "@/hooks/useDeployments";

const Dashboard = () => {
  const navigate = useNavigate();
  const { data: deployments = [], isLoading } = useDeployments();

  return (
    <DashboardLayout>
      <div className="p-6 lg:p-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-semibold text-foreground tracking-tight">
              My API Deployments
            </h1>
            <p className="text-sm text-muted-foreground mt-1">
              {deployments.length} services deployed
            </p>
          </div>
          <Button onClick={() => navigate("/new")} className="gap-2">
            <Plus className="h-4 w-4" />
            New API Deployment
          </Button>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-32 bg-card border border-border rounded-lg animate-pulse" />
            ))}
          </div>
        ) : deployments.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 border-2 border-dashed rounded-xl border-muted">
            <p className="text-muted-foreground mb-4">No deployments yet</p>
            <Button variant="outline" onClick={() => navigate("/new")} className="gap-2">
              <Plus className="h-4 w-4" />
              Create your first deployment
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {deployments.map((deployment) => (
              <ProjectCard key={deployment.deploymentId} deployment={deployment} />
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default Dashboard;
