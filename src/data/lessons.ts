export type Complexity = 'O(1)' | 'O(log n)' | 'O(n)' | 'O(n log n)' | 'O(n²)' | 'O(2ⁿ)'
export type LessonStep = { title: string; body: string; code?: string[]; activeLine?: number; values?: number[]; highlight?: number[] }
export type Lesson = { id: string; title: string; category: string; duration: string; description: string; steps: LessonStep[] }

export const complexities: { id: Complexity; label: string; detail: string; color: string; rank: number }[] = [
  { id: 'O(1)', label: 'Constant', detail: 'The work stays the same regardless of input size.', color: '#a78bfa', rank: 1 },
  { id: 'O(log n)', label: 'Logarithmic', detail: 'The problem shrinks by a constant factor each step.', color: '#60a5fa', rank: 2 },
  { id: 'O(n)', label: 'Linear', detail: 'Work grows in direct proportion to the input.', color: '#34d399', rank: 3 },
  { id: 'O(n log n)', label: 'Linearithmic', detail: 'A linear amount of work across logarithmic levels.', color: '#fbbf24', rank: 4 },
  { id: 'O(n²)', label: 'Quadratic', detail: 'Every item is compared with every other item.', color: '#fb923c', rank: 5 },
  { id: 'O(2ⁿ)', label: 'Exponential', detail: 'Each added input element roughly doubles the work.', color: '#f87171', rank: 6 },
]

export const lessons: Lesson[] = [
  { id: 'big-o', title: 'Big-O notation', category: 'Fundamentals', duration: '8 min', description: 'Understand how algorithm cost grows as input gets larger.', steps: [
    { title: 'What are we measuring?', body: 'Big-O describes how the number of operations grows as the input size n grows. It focuses on the shape of growth, not the exact milliseconds on one machine.', code: ['function first(items) {', '  return items[0]', '}'], activeLine: 2, values: [4, 8, 12, 16, 20] },
    { title: 'Constant time · O(1)', body: 'Reading the first array element takes one operation. The array could contain ten items or ten million; this operation does not scan the array.', code: ['function first(items) {', '  return items[0]', '}'], activeLine: 2, values: [4, 8, 12, 16, 20], highlight: [0] },
    { title: 'Linear time · O(n)', body: 'A loop that visits every item performs work proportional to n. Double the input and you roughly double the number of visits.', code: ['function sum(items) {', '  let total = 0', '  for (const item of items) {', '    total += item', '  }', '  return total', '}'], activeLine: 3, values: [4, 8, 12, 16, 20], highlight: [0,1,2,3,4] },
    { title: 'Quadratic time · O(n²)', body: 'A nested loop over the same collection can perform n × n comparisons. Growth becomes steep quickly, so look carefully for nested work.', code: ['for (let i = 0; i < n; i++) {', '  for (let j = 0; j < n; j++) {', '    compare(i, j)', '  }', '}'], activeLine: 2, values: [4, 8, 12, 16, 20] },
    { title: 'Compare the growth', body: 'The curves tell the story: constant and logarithmic algorithms scale gently, while quadratic and exponential algorithms become expensive as n increases.', code: ['// Compare operation counts', 'O(1)       // 1', 'O(log n)   // log₂(n)', 'O(n)       // n', 'O(n log n) // n log₂(n)', 'O(n²)      // n²'], activeLine: 4, values: [4, 8, 12, 16, 20] },
  ]},
  { id: 'arrays', title: 'Array traversal', category: 'Data structures', duration: '5 min', description: 'See how an index walks through contiguous values.', steps: [
    { title: 'Meet the array', body: 'An array stores ordered values. Each value can be accessed directly by its index, starting at zero.', code: ['const scores = [8, 3, 7, 1, 9]', 'scores[2] // 7'], activeLine: 1, values: [8,3,7,1,9] },
    { title: 'Move the pointer', body: 'A traversal advances an index one position at a time. Each step visits exactly one element.', code: ['for (let i = 0; i < scores.length; i++) {', '  visit(scores[i])', '}'], activeLine: 1, values: [8,3,7,1,9], highlight: [0] },
    { title: 'The cost is linear', body: 'To visit all n values, the loop runs n times. Time complexity is O(n), and the extra space is O(1).', code: ['// n visits → O(n) time', '// one index → O(1) space'], activeLine: 0, values: [8,3,7,1,9], highlight: [0,1,2,3,4] },
  ]},
  { id: 'binary-search', title: 'Binary search', category: 'Searching', duration: '6 min', description: 'Discard half the remaining search space on every step.', steps: [
    { title: 'Sorted input is required', body: 'Binary search only works when values are sorted. Start with pointers at the first and last positions.', code: ['const values = [2, 5, 8, 12, 16, 23, 38]', 'let low = 0', 'let high = values.length - 1'], activeLine: 0, values: [2,5,8,12,16,23,38] },
    { title: 'Inspect the midpoint', body: 'Calculate the middle index and compare its value with the target. Here we search for 16.', code: ['const mid = Math.floor((low + high) / 2)', 'if (values[mid] === target) return mid'], activeLine: 0, values: [2,5,8,12,16,23,38], highlight: [3] },
    { title: 'Discard half', body: 'If the target is larger, continue only in the right half. Otherwise continue in the left half. Repeat until found or the range is empty.', code: ['if (values[mid] < target) low = mid + 1', 'else high = mid - 1'], activeLine: 0, values: [2,5,8,12,16,23,38], highlight: [4,5,6] },
    { title: 'Logarithmic growth', body: 'Each iteration halves the remaining candidates. The number of steps grows as log₂(n), giving O(log n) time.', code: ['// 128 items → at most 7 halvings', '// Time: O(log n)', '// Space: O(1)'], activeLine: 1, values: [2,5,8,12,16,23,38] },
  ]},
  { id: 'bubble-sort', title: 'Bubble sort', category: 'Sorting', duration: '7 min', description: 'Watch adjacent comparisons move large values to the end.', steps: [
    { title: 'Start unsorted', body: 'Bubble sort repeatedly compares adjacent values and swaps them when they are in the wrong order.', code: ['const values = [5, 1, 4, 2, 8]'], activeLine: 0, values: [5,1,4,2,8] },
    { title: 'Compare neighbors', body: 'Compare the first pair. Since 5 is greater than 1, swap them. The largest values gradually bubble to the right.', code: ['if (values[j] > values[j + 1]) {', '  swap(values, j, j + 1)', '}'], activeLine: 0, values: [1,5,4,2,8], highlight: [0,1] },
    { title: 'Repeat passes', body: 'After each pass, the largest unsorted value is in its final position. Continue over the remaining prefix.', code: ['for (let end = n - 1; end > 0; end--) {', '  for (let j = 0; j < end; j++) {', '    // compare and swap', '  }', '}'], activeLine: 1, values: [1,4,2,5,8], highlight: [0,1,2,3,4] },
    { title: 'Know the trade-off', body: 'Bubble sort is easy to understand but takes O(n²) average and worst-case time. Its space complexity is O(1).', code: ['// Average time: O(n²)', '// Worst time:   O(n²)', '// Extra space:  O(1)'], activeLine: 0, values: [1,2,4,5,8] },
  ]},
]

export function operationCount(complexity: Complexity, n: number): number {
  switch (complexity) {
    case 'O(1)': return 1
    case 'O(log n)': return Math.log2(Math.max(1, n))
    case 'O(n)': return n
    case 'O(n log n)': return n * Math.log2(Math.max(1, n))
    case 'O(n²)': return n * n
    case 'O(2ⁿ)': return Math.pow(2, Math.min(n, 20))
  }
}