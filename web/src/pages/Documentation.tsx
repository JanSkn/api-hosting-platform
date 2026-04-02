import { DashboardLayout } from "@/components/DashboardLayout";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import docsContent from "@/docs/documentation.md?raw";

const Documentation = () => {
  return (
    <DashboardLayout>
      <div className="p-6 lg:p-8">
        <article className="prose prose-sm dark:prose-invert max-w-none prose-headings:text-foreground prose-p:text-muted-foreground prose-li:text-muted-foreground prose-strong:text-foreground prose-code:bg-muted prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:text-xs prose-code:font-mono prose-pre:bg-terminal prose-pre:text-terminal-foreground prose-th:text-foreground prose-td:text-muted-foreground prose-table:border prose-th:border prose-th:border-border prose-td:border prose-td:border-border prose-th:px-4 prose-th:py-2 prose-td:px-4 prose-td:py-2">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{docsContent}</ReactMarkdown>
        </article>
      </div>
    </DashboardLayout>
  );
};

export default Documentation;
