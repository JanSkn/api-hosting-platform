import { DashboardLayout } from "@/components/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Separator } from "@/components/ui/separator";
import { useCurrentUser, useDeleteAccount } from "@/hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { useToast } from "@/hooks/use-toast";
import { Loader2 } from "lucide-react";

const Settings = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { data: user } = useCurrentUser();
  const deleteAccount = useDeleteAccount();

  const handleDeleteAccount = () => {
    if (!confirm("Are you sure? This action is irreversible.")) return;
    deleteAccount.mutate(undefined, {
      onSuccess: () => navigate("/login"),
      onError: () => toast({ title: "Failed to delete account", variant: "destructive" }),
    });
  };

  return (
    <DashboardLayout>
      <div className="p-6 lg:p-8 max-w-3xl">
        <h1 className="text-2xl font-semibold text-foreground tracking-tight mb-6">Settings</h1>

        <div className="space-y-8">
          <section className="space-y-4">
            <h2 className="text-lg font-medium text-foreground">Profile</h2>
            <div className="space-y-3">
              <div className="space-y-1.5">
                <Label>Display Name</Label>
                <p className="text-sm text-foreground">{user?.name ?? "John Doe"}</p>
              </div>
              <div className="space-y-1.5">
                <Label>Email</Label>
                <p className="text-sm text-foreground">{user?.email ?? "john@example.com"}</p>
              </div>
            </div>
          </section>

          <Separator />

          <section className="space-y-4">
            <h2 className="text-lg font-medium text-foreground">Notifications</h2>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-foreground">Deploy alerts</p>
                <p className="text-xs text-muted-foreground">Get notified when a deployment fails</p>
              </div>
              <Switch defaultChecked />
            </div>
          </section>

          <Separator />

          <section className="space-y-4">
            <h2 className="text-lg font-medium text-destructive">Danger Zone</h2>
            <div className="border border-destructive/20 rounded-lg p-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-foreground">Delete Account</p>
                <p className="text-xs text-muted-foreground">Permanently remove your account and all data</p>
              </div>
              <Button variant="destructive" size="sm" onClick={handleDeleteAccount} disabled={deleteAccount.isPending}>
                {deleteAccount.isPending ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                Delete
              </Button>
            </div>
          </section>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Settings;
