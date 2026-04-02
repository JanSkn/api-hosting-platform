import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  Settings,
  FileCode,
  ChevronLeft,
  ChevronRight,
  Zap,
} from "lucide-react";
import { cn } from "@/lib/utils";

const navItems = [
  { title: "Dashboard", icon: LayoutDashboard, path: "/" },
  { title: "Documentation", icon: FileCode, path: "/docs" },
  { title: "Settings", icon: Settings, path: "/settings" },
];

export function AppSidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const location = useLocation();

  return (
    <aside
      className={cn(
        "h-screen sticky top-0 flex flex-col border-r border-border bg-surface transition-all duration-200 overflow-hidden",
        collapsed ? "w-16" : "w-60"
      )}
    >
      <div className="flex items-center gap-2 px-4 h-14 border-b border-border">
        <Zap className="h-6 w-6 text-primary shrink-0" />
        {!collapsed && (
          <span className="font-semibold text-foreground text-lg tracking-tight">
            APIForge
          </span>
        )}
      </div>

      <nav className="flex-1 py-4 px-2 space-y-1">
        {navItems.map((item) => {
          const active = location.pathname === item.path;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                "flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-100",
                active
                  ? "bg-primary/10 text-primary"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {!collapsed && <span>{item.title}</span>}
            </Link>
          );
        })}
      </nav>

      <div
        className={cn(
          "mt-auto w-full flex justify-center py-4 transition-opacity duration-300",
          collapsed ? "opacity-0 pointer-events-none" : "opacity-100 delay-200"
        )}
      >
        <a
          href="https://apiforge.betteruptime.com"
          target="_blank"
          rel="noreferrer"
          className="flex items-center gap-2 px-3 py-1.5 rounded-full border border-border bg-background hover:bg-accent transition-colors text-xs font-medium text-muted-foreground hover:text-foreground shadow-sm"
        >
          <span className="relative flex h-2 w-2 shrink-0">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="whitespace-nowrap">APIForge System Status</span>
        </a>
      </div>

      <button
        onClick={() => setCollapsed(!collapsed)}
        className="flex items-center justify-center h-10 border-t border-border text-muted-foreground hover:text-foreground transition-colors"
      >
        {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
      </button>
    </aside>
  );
}
