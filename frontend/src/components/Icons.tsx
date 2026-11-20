import type { SVGProps } from 'react'

type Props = { size?: number }

const base = (size: number): SVGProps<SVGSVGElement> => ({
  width: size,
  height: size,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.8,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  'aria-hidden': true,
  focusable: false,
})

export function IconePolegar({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M7 10v11H4a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1h3Z" />
      <path d="M7 10l4-7a2.5 2.5 0 0 1 2.4 2.5V9h4.4a2 2 0 0 1 2 2.3l-1.2 7A2 2 0 0 1 16.6 21H7" />
    </svg>
  )
}

export function IconePolegarBaixo({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M17 14V3h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1h-3Z" />
      <path d="M17 14l-4 7a2.5 2.5 0 0 1-2.4-2.5V15H6.2a2 2 0 0 1-2-2.3l1.2-7A2 2 0 0 1 7.4 3H17" />
    </svg>
  )
}

export function IconeMais({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M12 5v14M5 12h14" />
    </svg>
  )
}

export function IconeX({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M18 6 6 18M6 6l12 12" />
    </svg>
  )
}

export function IconeBusca({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-3.5-3.5" />
    </svg>
  )
}

export function IconeSacola({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M6 7h12l1 12H5L6 7Z" />
      <path d="M9 7V6a3 3 0 0 1 6 0v1" />
    </svg>
  )
}

export function IconeRelogio({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 1.8" />
    </svg>
  )
}

export function IconeTrofeu({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M8 4h8v5a4 4 0 0 1-8 0V4Z" />
      <path d="M8 5.5H5.5a2.5 2.5 0 0 0 2.5 5M16 5.5h2.5a2.5 2.5 0 0 1-2.5 5" />
      <path d="M12 13v4M8.5 21h7l-1-3h-5l-1 3Z" />
    </svg>
  )
}

export function IconeSteam({ size = 18 }: Props) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden focusable="false">
      <path d="M11.98 2C6.6 2 2.18 6.13 1.75 11.4l5.2 2.15a2.9 2.9 0 0 1 1.72-.5l.24.01 2.31-3.35v-.05a3.87 3.87 0 1 1 3.87 3.87h-.09l-3.3 2.36c0 .06.01.12.01.19a2.9 2.9 0 1 1-5.79-.03L1.9 14.6C2.86 19.02 6.8 22.3 11.5 22.3c5.52 0 10-4.48 10-10s-4.48-10-10-10Zm-3.6 15.3-1.2-.5a2.18 2.18 0 0 0 3.47.99 2.18 2.18 0 0 0 .65-2.57 2.17 2.17 0 0 0-2.8-1.13l1.24.51a1.6 1.6 0 1 1-1.25 2.95l-.11-.05Zm9.87-8.45a2.58 2.58 0 1 0-5.17 0 2.58 2.58 0 0 0 5.17 0Zm-.45 0a2.13 2.13 0 1 1-4.26 0 2.13 2.13 0 0 1 4.26 0Z" />
    </svg>
  )
}

export function IconeSair({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M14 4h4a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-4" />
      <path d="M10 8l-4 4 4 4M18 12H6" />
    </svg>
  )
}

export function IconeSeta({ size = 18 }: Props) {
  return (
    <svg {...base(size)}>
      <path d="M7 17 17 7M9 7h8v8" />
    </svg>
  )
}
