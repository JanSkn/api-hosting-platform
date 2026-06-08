const http = require('http');

const server = http.createServer((req, res) => {
  const url = req.url;
  res.writeHead(200, { 'Content-Type': 'application/json' });

  if (url === '/hello') {
    res.end(JSON.stringify({ message: 'Hello World from Node.js 20!' }));
  } else {
    res.end(JSON.stringify({
      status: 'alive',
      runtime: 'Node.js 20',
      message: 'Welcome to your deployed API!',
      timestamp: new Date().toISOString()
    }));
  }
});

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
