import { DashboardLayout } from "@/components/DashboardLayout";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import docsContent from "@/docs/documentation.md?raw";

// highlight.js theme – GitHub Dark (works great on both light & dark backgrounds)
import "highlight.js/styles/github-dark.css";

const Documentation = () => {
  return (
    <DashboardLayout>
      <div className="p-6 lg:p-8">
        <article className="docs-prose prose prose-sm dark:prose-invert max-w-none">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeHighlight]}
          >
            {docsContent}
          </ReactMarkdown>
        </article>
      </div>
    </DashboardLayout>
  );
};

export default Documentation;
