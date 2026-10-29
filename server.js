const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PDF_PATH = path.join(__dirname, 'SPORTMEET_PROJECT_DOCUMENTATION.pdf');
const HTML_PATH = path.join(__dirname, 'SPORTMEET_PROJECT_DOCUMENTATION.html');

const server = http.createServer((req, res) => {
    const url = req.url.split('?')[0];

    if (url === '/download' || url === '/SPORTMEET_PROJECT_DOCUMENTATION.pdf') {
        if (!fs.existsSync(PDF_PATH)) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            return res.end('PDF not found. Please generate it first.');
        }
        const stat = fs.statSync(PDF_PATH);
        res.writeHead(200, {
            'Content-Type': 'application/pdf',
            'Content-Length': stat.size,
            'Content-Disposition': 'attachment; filename="SPORTMEET_PROJECT_DOCUMENTATION.pdf"'
        });
        const readStream = fs.createReadStream(PDF_PATH);
        readStream.pipe(res);
    } else if (url === '/view-pdf' || url === '/pdf') {
        if (!fs.existsSync(PDF_PATH)) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            return res.end('PDF not found.');
        }
        const stat = fs.statSync(PDF_PATH);
        res.writeHead(200, {
            'Content-Type': 'application/pdf',
            'Content-Length': stat.size,
            'Content-Disposition': 'inline; filename="SPORTMEET_PROJECT_DOCUMENTATION.pdf"'
        });
        const readStream = fs.createReadStream(PDF_PATH);
        readStream.pipe(res);
    } else {
        // Serve the HTML documentation
        if (fs.existsSync(HTML_PATH)) {
            const content = fs.readFileSync(HTML_PATH, 'utf-8');
            res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
            res.end(content);
        } else {
            res.writeHead(200, { 'Content-Type': 'text/plain' });
            res.end('Sports Arena Documentation Server Ready.');
        }
    }
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Documentation server listening on http://0.0.0.0:${PORT}`);
});
