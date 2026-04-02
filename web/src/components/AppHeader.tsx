import { Search, Bell, LogOut, Loader2, CheckCircle2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { useCurrentUser, useLogout } from "@/hooks/useAuth";
import { useNotifications, useMarkNotificationsRead } from "@/hooks/useNotifications";

export function AppHeader() {
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const logoutMutation = useLogout();
  const { data: notifications = [] } = useNotifications();
  const markRead = useMarkNotificationsRead();

  const unreadCount = notifications.filter((n) => !n.read).length;

  const initials = user?.name
    ? user.name.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2)
    : "JD";

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => navigate("/login"),
    });
  };

  return (
    <header className="h-14 border-b border-border bg-surface flex items-center justify-between px-6 sticky top-0 z-10">
      <div className="relative w-80">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <input
          type="text"
          placeholder="Search deployments..."
          className="w-full h-9 pl-9 pr-4 rounded-md border border-border bg-background text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring/20 transition-shadow"
        />
      </div>

      <div className="flex items-center gap-3">
        <Popover onOpenChange={(open) => { if (open && unreadCount > 0) markRead.mutate(); }}>
          <PopoverTrigger asChild>
            <button className="relative p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors">
              <Bell className="h-4 w-4" />
              {unreadCount > 0 && (
                <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-primary rounded-full" />
              )}
            </button>
          </PopoverTrigger>
          <PopoverContent align="end" className="w-80 p-0">
            <div className="px-4 py-3 border-b border-border">
              <h3 className="text-sm font-medium text-foreground">Notifications</h3>
            </div>
            {notifications.length === 0 ? (
              <div className="px-4 py-6 text-center text-sm text-muted-foreground">
                No notifications yet
              </div>
            ) : (
              <div className="max-h-64 overflow-y-auto">
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => n.deploymentId && navigate(`/deployment/${n.deploymentId}`)}
                    className="flex items-start gap-3 px-4 py-3 border-b border-border last:border-0 hover:bg-accent/50 cursor-pointer transition-colors"
                  >
                    <CheckCircle2 className="h-4 w-4 text-status-live shrink-0 mt-0.5" />
                    <div className="min-w-0">
                      <p className="text-sm text-foreground truncate">{n.message}</p>
                      <p className="text-xs text-muted-foreground">{n.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </PopoverContent>
        </Popover>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex items-center gap-2 hover:bg-accent rounded-md px-2 py-1 transition-colors">
              <Avatar className="h-7 w-7">
                <AvatarFallback className="bg-primary text-primary-foreground text-xs font-medium">
                  {initials}
                </AvatarFallback>
              </Avatar>
              <span className="text-sm font-medium text-foreground hidden sm:inline">
                {user?.name ?? "John Doe"}
              </span>
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-40">
            <DropdownMenuItem onClick={handleLogout} className="text-destructive" disabled={logoutMutation.isPending}>
              {logoutMutation.isPending ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <LogOut className="h-4 w-4 mr-2" />}
              Sign Out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}