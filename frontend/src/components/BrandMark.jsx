import { classNames } from '../utils/classNames';
import Icon from './Icon';

export default function BrandMark({ compact = false, className = '' }) {
  return (
    <div className={classNames('brand-mark', compact && 'brand-mark--compact', className)}>
      <span className="brand-mark__glyph">
        <Icon name="spark" size={compact ? 18 : 22} />
      </span>
      <div className="brand-mark__copy">
        <strong>CampusSphere</strong>
        {!compact && <span>One platform for every event across every college.</span>}
      </div>
    </div>
  );
}
