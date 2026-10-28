/**
 * Line icons drawn inline (no icon library). Every icon is decorative: the word next to it carries the meaning,
 * so status never relies on an icon or a colour alone (role file 02 §12).
 */
import type { SVGProps } from "react";

type IconProps = SVGProps<SVGSVGElement> & { size?: number };

function Icon({ size = 20, children, ...props }: IconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      {children}
    </svg>
  );
}

export const ArrowLeftIcon = (props: IconProps) => (
  <Icon {...props}><path d="M19 12H5M11 18l-6-6 6-6" /></Icon>
);
export const ArrowRightIcon = (props: IconProps) => (
  <Icon {...props}><path d="M5 12h14M13 6l6 6-6 6" /></Icon>
);
export const BellIcon = (props: IconProps) => (
  <Icon {...props}><path d="M6 9a6 6 0 1 1 12 0c0 6 2.5 7.5 2.5 7.5h-17S6 15 6 9M10 20a2.2 2.2 0 0 0 4 0" /></Icon>
);
export const UserIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="8" r="4" /><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" /></Icon>
);
export const CheckIcon = (props: IconProps) => (
  <Icon {...props}><path d="M5 12.5l4.5 4.5L19 7.5" /></Icon>
);
export const CheckCircleIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="12" r="9" /><path d="M8 12.5l2.8 2.8L16.5 9.5" /></Icon>
);
export const ClockIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3.5 2" /></Icon>
);
export const WarningIcon = (props: IconProps) => (
  <Icon {...props}><path d="M12 3.5 2.5 20h19L12 3.5zM12 10v4.5M12 17.5v.01" /></Icon>
);
export const XCircleIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="12" r="9" /><path d="M9 9l6 6M15 9l-6 6" /></Icon>
);
export const InfoIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="12" r="9" /><path d="M12 11v5.5M12 7.5v.01" /></Icon>
);
export const TrendUpIcon = (props: IconProps) => (
  <Icon {...props}><path d="M3 17l6-6 4 4 8-8M15 7h6v6" /></Icon>
);
export const TrendDownIcon = (props: IconProps) => (
  <Icon {...props}><path d="M3 7l6 6 4-4 8 8M15 17h6v-6" /></Icon>
);
export const TrendFlatIcon = (props: IconProps) => (
  <Icon {...props}><path d="M3 12h16M15 8l4 4-4 4" /></Icon>
);
export const MinusCircleIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="12" cy="12" r="9" /><path d="M8 12h8" /></Icon>
);
export const EyeIcon = (props: IconProps) => (
  <Icon {...props}><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" /><circle cx="12" cy="12" r="3" /></Icon>
);
export const EyeOffIcon = (props: IconProps) => (
  <Icon {...props}><path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.9 5.2A10.4 10.4 0 0 1 12 5c6.5 0 10 7 10 7a17.6 17.6 0 0 1-3.2 4.2M6.6 6.6C3.7 8.5 2 12 2 12s3.5 7 10 7a10 10 0 0 0 5.4-1.6" /></Icon>
);
export const PlusIcon = (props: IconProps) => (
  <Icon {...props}><path d="M12 5v14M5 12h14" /></Icon>
);
export const TrashIcon = (props: IconProps) => (
  <Icon {...props}><path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3" /></Icon>
);
export const SearchIcon = (props: IconProps) => (
  <Icon {...props}><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></Icon>
);
export const ChevronDownIcon = (props: IconProps) => (
  <Icon {...props}><path d="M6 9l6 6 6-6" /></Icon>
);
export const ChevronRightIcon = (props: IconProps) => (
  <Icon {...props}><path d="M9 6l6 6-6 6" /></Icon>
);
export const TruckIcon = (props: IconProps) => (
  <Icon {...props}><path d="M2 6h11v10H2zM13 10h4l3 3v3h-7" /><circle cx="6" cy="17.5" r="1.8" /><circle cx="17" cy="17.5" r="1.8" /></Icon>
);
export const SlidersIcon = (props: IconProps) => (
  <Icon {...props}><path d="M4 6h10M18 6h2M4 12h4M12 12h8M4 18h12M20 18h0" /><circle cx="16" cy="6" r="2" /><circle cx="10" cy="12" r="2" /><circle cx="18" cy="18" r="2" /></Icon>
);
export const RefreshIcon = (props: IconProps) => (
  <Icon {...props}><path d="M20 11a8 8 0 0 0-14.6-4.5L3 9M4 13a8 8 0 0 0 14.6 4.5L21 15M3 4v5h5M21 20v-5h-5" /></Icon>
);
export const LightbulbIcon = (props: IconProps) => (
  <Icon {...props}><path d="M9 18h6M10 21h4M12 3a6 6 0 0 0-3.5 10.9c.6.5 1 1.2 1 2V16h5v-.1c0-.8.4-1.5 1-2A6 6 0 0 0 12 3z" /></Icon>
);
export const DocumentIcon = (props: IconProps) => (
  <Icon {...props}><path d="M6 3h8l4 4v14H6zM14 3v4h4M9 12h6M9 16h6" /></Icon>
);
export const LanguageIcon = (props: IconProps) => (
  <Icon {...props}><path d="M4 5h9M8.5 3v2M6 5c.5 3 2.5 6 6 8M11 5c-.5 4-3 7-7 9M13 21l4-10 4 10M14.5 17.5h5" /></Icon>
);
