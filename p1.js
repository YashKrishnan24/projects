import { useState, useRef, useEffect } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";

const COMPLEXITY_PATTERNS = [
  {
    label: "O(1) — Constant",
    regex: [/return\s+\w+\[0\]/i, /return\s+\w+\s*[+\-*/%]\s*\w+/i, /\.length/i],
    keywords: ["direct access", "array[0]", "hash map get", "return constant"],
    color: "#00ff88",
    fn: (n) => 1,
    description: "Doesn't grow with input. Best possible.",
  },
  {
    label: "O(log n) — Logarithmic",
    regex: [/\/\/\s*2|mid\s*=|binary/i, /left\s*=\s*mid|right\s*=\s*mid/i],
    keywords: ["binary search", "divide", "mid", "half"],
    color: "#00d4ff",
    fn: (n) => Math.log2(n),
    description: "Halves the problem each step. Very efficient.",
  },
  {
    label: "O(n) — Linear",
    regex: [/for\s*\(.*\b[a-z]\b.*<.*\blength\b/i, /while\s*\(\s*\w+\s*[<>]/i, /\.forEach|\.map\(|\.filter\(/i],
    keywords: ["single loop", "linear scan", "traverse", "forEach"],
    color: "#ffd700",
    fn: (n) => n,
    description: "Grows linearly. Common and acceptable.",
  },
  {
    label: "O(n log n) — Linearithmic",
    regex: [/mergeSort|merge_sort|quickSort|quick_sort/i, /\.sort\(/i, /divide.*conquer/i],
    keywords: ["merge sort", "quick sort", "heap sort", ".sort()", "divide and conquer"],
    color: "#ff9f43",
    fn: (n) => n * Math.log2(n),
    description: "Efficient sorting algorithms live here.",
  },
  {
    label: "O(n²) — Quadratic",
    regex: [/for.*\n.*for|for.*{[^}]*for/s, /\bfor\b.*\bfor\b/i],
    keywords: ["nested loops", "bubble sort", "selection sort", "matrix"],
    color: "#ff6b6b",
    fn: (n) => n * n,
    description: "Nested loops. Avoid for large inputs.",
  },
  {
    label: "O(2ⁿ) — Exponential",
    regex: [/return\s+\w+\s*\(\s*\w+\s*-\s*1\s*\)\s*\+\s*\w+\s*\(\s*\w+\s*-\s*2\s*\)/i, /fibonacci|fib/i, /subset|power_set/i],
    keywords: ["fibonacci recursive", "subsets", "power set", "brute force"],
    color: "#ff4757",
    fn: (n) => Math.min(Math.pow(2, n), 1e15),
    description: "Doubles with each input. Very dangerous.",
  },
];

const EXAMPLES = {
  "Binary Search": `function binarySearch(arr, target) {
  let left = 0, right = arr.length - 1;
  while (left <= right) {
    let mid = Math.floor((left + right) / 2);
    if (arr[mid] === target) return mid;
    else if (arr[mid] < target) left = mid + 1;
    else right = mid - 1;
  }
  return -1;
}`,
  "Bubble Sort": `function bubbleSort(arr) {
  for (let i = 0; i < arr.length; i++) {
    for (let j = 0; j < arr.length - i - 1; j++) {
      if (arr[j] > arr[j + 1]) {
        let temp = arr[j];
        arr[j] = arr[j + 1];
        arr[j + 1] = temp;
      }
    }
  }
  return arr;
}`,
  "Fibonacci Recursive": `function fibonacci(n) {
  if (n <= 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
}`,
  "Merge Sort": `function mergeSort(arr) {
  if (arr.length <= 1) return arr;
  const mid = Math.floor(arr.length / 2);
  const left = mergeSort(arr.slice(0, mid));
  const right = mergeSort(arr.slice(mid));
  return merge(left, right);
}`,
  "Array Sum": `function arraySum(arr) {
  let sum = 0;
  arr.forEach(num => sum += num);
  return sum;
}`,
};

function analyzeCode(code) {
  const lines = code.split("\n");
  const results = [];

  // Count loop nesting depth
  let maxNesting = 0, currentNesting = 0;
  for (const line of lines) {
    if (/\bfor\b|\bwhile\b/.test(line)) currentNesting++;
    if (/}/.test(line)) currentNesting = Math.max(0, currentNesting - 1);
    maxNesting = Math.max(maxNesting, currentNesting);
  }

  // Match patterns
  for (const pattern of COMPLEXITY_PATTERNS) {
    let score = 0;
    for (const reg of pattern.regex) {
      if (reg.test(code)) score += 2;
    }
    for (const kw of pattern.keywords) {
      if (code.toLowerCase().includes(kw.toLowerCase())) score += 1;
    }
    if (score > 0) results.push({ ...pattern, score });
  }

  // Nesting heuristic
  if (maxNesting >= 2 && !results.find(r => r.label.includes("n²"))) {
    const quad = COMPLEXITY_PATTERNS.find(p => p.label.includes("n²"));
    results.push({ ...quad, score: maxNesting * 2 });
  }

  results.sort((a, b) => b.score - a.score);

  const best = results[0] || COMPLEXITY_PATTERNS[2]; // default O(n)
  const spaceComplexity =
    /new Array|new Map|new Set|\.push|stack|queue|memo|dp\[/i.test(code)
      ? "O(n)"
      : /matrix|\[\]\[\]|grid/i.test(code)
      ? "O(n²)"
      : "O(1)";

  return { timeComplexity: best, spaceComplexity, allMatches: results.slice(0, 3) };
}

function generateChartData(complexities) {
  const points = [1, 2, 4, 8, 16, 32, 64, 128, 256, 512];
  return points.map((n) => {
    const entry = { n };
    for (const c of complexities) {
      const val = c.fn(n);
      entry[c.label] = isFinite(val) ? Math.round(val * 100) / 100 : null;
    }
    return entry;
  });
}

const Badge = ({ label, color }) => (
  <span style={{
    background: color + "22",
    color: color,
    border: `1px solid ${color}55`,
    borderRadius: 6,
    padding: "2px 10px",
    fontSize: 12,
    fontFamily: "'JetBrains Mono', monospace",
    fontWeight: 600,
  }}>{label}</span>
);

export default function App() {
  const [code, setCode] = useState(EXAMPLES["Binary Search"]);
  const [result, setResult] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [activeExample, setActiveExample] = useState("Binary Search");
  const [chartData, setChartData] = useState([]);
  const [showAll, setShowAll] = useState(false);

  const handleAnalyze = () => {
    setAnalyzing(true);
    setTimeout(() => {
      const res = analyzeCode(code);
      setResult(res);
      setChartData(generateChartData(COMPLEXITY_PATTERNS));
      setAnalyzing(false);
    }, 600);
  };

  useEffect(() => {
    handleAnalyze();
  }, []);

  return (
    <div style={{
      minHeight: "100vh",
      background: "#0a0a0f",
      color: "#e8e8f0",
      fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
      padding: "0",
    }}>
      {/* Header */}
      <div style={{
        borderBottom: "1px solid #1e1e2e",
        background: "linear-gradient(135deg, #0d0d1a 0%, #0a0a0f 100%)",
        padding: "24px 40px",
        display: "flex",
        alignItems: "center",
        gap: 16,
      }}>
        <div style={{
          width: 40, height: 40,
          background: "linear-gradient(135deg, #00ff88, #00d4ff)",
          borderRadius: 10,
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: 20,
        }}>Ω</div>
        <div>
          <div style={{ fontSize: 20, fontWeight: 700, letterSpacing: "-0.5px", color: "#fff" }}>
            BigO Analyzer
          </div>
          <div style={{ fontSize: 12, color: "#666", marginTop: 2 }}>
            Time & Space Complexity — CSE Student Toolkit
          </div>
        </div>
        <div style={{ marginLeft: "auto", display: "flex", gap: 8 }}>
          {["O(1)", "O(log n)", "O(n)", "O(n²)"].map((label, i) => (
            <span key={i} style={{
              fontSize: 11,
              color: ["#00ff88", "#00d4ff", "#ffd700", "#ff6b6b"][i],
              background: ["#00ff8811", "#00d4ff11", "#ffd70011", "#ff6b6b11"][i],
              border: `1px solid ${["#00ff8833", "#00d4ff33", "#ffd70033", "#ff6b6b33"][i]}`,
              borderRadius: 4,
              padding: "3px 8px",
            }}>{label}</span>
          ))}
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 0, minHeight: "calc(100vh - 90px)" }}>
        {/* Left: Editor */}
        <div style={{ borderRight: "1px solid #1e1e2e", display: "flex", flexDirection: "column" }}>
          {/* Example tabs */}
          <div style={{
            display: "flex", overflowX: "auto", gap: 0,
            borderBottom: "1px solid #1e1e2e",
            background: "#0d0d1a",
            padding: "0 16px",
          }}>
            {Object.keys(EXAMPLES).map((name) => (
              <button key={name} onClick={() => {
                setActiveExample(name);
                setCode(EXAMPLES[name]);
                setResult(null);
              }} style={{
                background: "none",
                border: "none",
                borderBottom: activeExample === name ? "2px solid #00ff88" : "2px solid transparent",
                color: activeExample === name ? "#00ff88" : "#555",
                padding: "10px 14px",
                cursor: "pointer",
                fontSize: 11,
                whiteSpace: "nowrap",
                fontFamily: "inherit",
                transition: "color 0.2s",
              }}>{name}</button>
            ))}
          </div>

          {/* Code editor */}
          <div style={{ position: "relative", flex: 1 }}>
            <div style={{
              position: "absolute", top: 12, left: 12,
              fontSize: 10, color: "#333", userSelect: "none",
              lineHeight: "22px", textAlign: "right", width: 24,
              paddingTop: 4,
            }}>
              {code.split("\n").map((_, i) => (
                <div key={i}>{i + 1}</div>
              ))}
            </div>
            <textarea
              value={code}
              onChange={(e) => { setCode(e.target.value); setResult(null); }}
              spellCheck={false}
              style={{
                width: "100%", height: "100%",
                minHeight: 380,
                background: "#0d0d1a",
                color: "#c8d3f5",
                border: "none",
                outline: "none",
                resize: "none",
                fontFamily: "'JetBrains Mono', monospace",
                fontSize: 13,
                lineHeight: "22px",
                padding: "12px 12px 12px 52px",
                boxSizing: "border-box",
              }}
            />
          </div>

          {/* Analyze button */}
          <div style={{ padding: "16px 20px", borderTop: "1px solid #1e1e2e", background: "#0d0d1a" }}>
            <button onClick={handleAnalyze} disabled={analyzing} style={{
              width: "100%",
              padding: "12px",
              background: analyzing ? "#1e1e2e" : "linear-gradient(135deg, #00ff88, #00d4ff)",
              color: analyzing ? "#555" : "#000",
              border: "none",
              borderRadius: 8,
              fontSize: 13,
              fontWeight: 700,
              fontFamily: "inherit",
              cursor: analyzing ? "default" : "pointer",
              transition: "all 0.3s",
              letterSpacing: "0.5px",
            }}>
              {analyzing ? "⚙ Analyzing..." : "▶ ANALYZE COMPLEXITY"}
            </button>
          </div>
        </div>

        {/* Right: Results */}
        <div style={{ overflowY: "auto", display: "flex", flexDirection: "column" }}>
          {result ? (
            <>
              {/* Main result */}
              <div style={{ padding: "24px 28px", borderBottom: "1px solid #1e1e2e" }}>
                <div style={{ fontSize: 11, color: "#555", marginBottom: 12, letterSpacing: 2, textTransform: "uppercase" }}>
                  Detected Complexity
                </div>
                <div style={{
                  display: "flex", alignItems: "center", gap: 16,
                  padding: "20px 24px",
                  background: result.timeComplexity.color + "0a",
                  border: `1px solid ${result.timeComplexity.color}33`,
                  borderRadius: 12,
                  marginBottom: 12,
                }}>
                  <div style={{
                    width: 56, height: 56,
                    background: result.timeComplexity.color + "22",
                    border: `2px solid ${result.timeComplexity.color}`,
                    borderRadius: 12,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 22, color: result.timeComplexity.color,
                    flexShrink: 0,
                  }}>T</div>
                  <div>
                    <div style={{ fontSize: 22, fontWeight: 700, color: result.timeComplexity.color }}>
                      {result.timeComplexity.label}
                    </div>
                    <div style={{ fontSize: 12, color: "#888", marginTop: 4 }}>
                      {result.timeComplexity.description}
                    </div>
                  </div>
                </div>

                <div style={{
                  display: "flex", alignItems: "center", gap: 16,
                  padding: "14px 20px",
                  background: "#1a1a2e",
                  border: "1px solid #2a2a3e",
                  borderRadius: 10,
                }}>
                  <div style={{
                    width: 40, height: 40,
                    background: "#7c3aed22",
                    border: "2px solid #7c3aed",
                    borderRadius: 8,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 16, color: "#a78bfa", flexShrink: 0,
                  }}>S</div>
                  <div>
                    <div style={{ fontSize: 14, fontWeight: 600, color: "#a78bfa" }}>
                      Space: {result.spaceComplexity}
                    </div>
                    <div style={{ fontSize: 11, color: "#666", marginTop: 2 }}>
                      Memory usage estimate
                    </div>
                  </div>
                </div>
              </div>

              {/* All matches */}
              {result.allMatches.length > 1 && (
                <div style={{ padding: "16px 28px", borderBottom: "1px solid #1e1e2e" }}>
                  <div style={{ fontSize: 11, color: "#555", marginBottom: 10, letterSpacing: 2, textTransform: "uppercase" }}>
                    Pattern Confidence
                  </div>
                  {result.allMatches.map((m, i) => (
                    <div key={i} style={{
                      display: "flex", alignItems: "center", gap: 10, marginBottom: 8,
                    }}>
                      <div style={{ width: 90, fontSize: 11, color: m.color }}>{m.label.split("—")[0].trim()}</div>
                      <div style={{
                        flex: 1, height: 6, background: "#1e1e2e", borderRadius: 3, overflow: "hidden",
                      }}>
                        <div style={{
                          height: "100%",
                          width: `${Math.min(100, (m.score / 6) * 100)}%`,
                          background: m.color,
                          borderRadius: 3,
                          transition: "width 0.5s ease",
                        }} />
                      </div>
                      <div style={{ fontSize: 10, color: "#555", width: 30 }}>{m.score}/6</div>
                    </div>
                  ))}
                </div>
              )}

              {/* Complexity Reference */}
              <div style={{ padding: "16px 28px", borderBottom: "1px solid #1e1e2e" }}>
                <div style={{
                  display: "flex", justifyContent: "space-between", alignItems: "center",
                  marginBottom: 10,
                }}>
                  <div style={{ fontSize: 11, color: "#555", letterSpacing: 2, textTransform: "uppercase" }}>
                    Complexity Scale
                  </div>
                  <button onClick={() => setShowAll(!showAll)} style={{
                    background: "none", border: "none", color: "#444",
                    fontSize: 10, cursor: "pointer", fontFamily: "inherit",
                  }}>{showAll ? "▲ less" : "▼ all"}</button>
                </div>
                {(showAll ? COMPLEXITY_PATTERNS : COMPLEXITY_PATTERNS.slice(0, 4)).map((p, i) => (
                  <div key={i} style={{
                    display: "flex", alignItems: "center", gap: 10,
                    padding: "6px 0",
                    opacity: p.label === result.timeComplexity.label ? 1 : 0.5,
                    borderLeft: p.label === result.timeComplexity.label ? `3px solid ${p.color}` : "3px solid transparent",
                    paddingLeft: 10,
                    transition: "opacity 0.3s",
                  }}>
                    <div style={{ width: 100, fontSize: 12, color: p.color, fontWeight: 600 }}>{p.label.split("—")[0]}</div>
                    <div style={{ fontSize: 11, color: "#666" }}>{p.description}</div>
                  </div>
                ))}
              </div>

              {/* Chart */}
              <div style={{ padding: "16px 28px", flex: 1 }}>
                <div style={{ fontSize: 11, color: "#555", marginBottom: 14, letterSpacing: 2, textTransform: "uppercase" }}>
                  Growth Visualization
                </div>
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e1e2e" />
                    <XAxis dataKey="n" stroke="#333" tick={{ fill: "#555", fontSize: 10 }} />
                    <YAxis stroke="#333" tick={{ fill: "#555", fontSize: 10 }} domain={[0, 100]} />
                    <Tooltip
                      contentStyle={{ background: "#0d0d1a", border: "1px solid #2a2a3e", borderRadius: 8, fontSize: 11 }}
                      labelStyle={{ color: "#888" }}
                    />
                    {COMPLEXITY_PATTERNS.slice(0, 4).map((p) => (
                      <Line
                        key={p.label}
                        type="monotone"
                        dataKey={p.label}
                        stroke={p.color}
                        strokeWidth={p.label === result.timeComplexity.label ? 3 : 1}
                        dot={false}
                        opacity={p.label === result.timeComplexity.label ? 1 : 0.3}
                      />
                    ))}
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </>
          ) : (
            <div style={{
              flex: 1, display: "flex", alignItems: "center", justifyContent: "center",
              flexDirection: "column", gap: 12, color: "#333",
            }}>
              <div style={{ fontSize: 48 }}>Ω</div>
              <div style={{ fontSize: 14 }}>Paste your code and click Analyze</div>
            </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div style={{
        borderTop: "1px solid #1e1e2e",
        padding: "10px 40px",
        display: "flex",
        alignItems: "center",
        gap: 20,
        background: "#0d0d1a",
        fontSize: 11,
        color: "#444",
      }}>
        <span>Supports: JavaScript · Python · Java syntax patterns</span>
        <span style={{ marginLeft: "auto" }}>Built for CSE students 🎓</span>
      </div>
    </div>
  );
}
